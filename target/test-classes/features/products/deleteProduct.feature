@productDelete
Feature: suppression d'un produit de base de notre site
  Background:
    * url 'http://localhost:8080'
    * def adminToken = 'Bearer admin-token'
    * def unauthorizedToken = 'Bearer unauthorized-token'

  @deleteProduct @valid
  Scenario: Suppression valide d'un produit de base
    Given path 'products/1'
    And headers { 'Authorization': '#(adminToken)' }
    When method Delete
    Then status 204

  @deleteProduct @valid
  Scenario: Suppression valide d'un produit avec des dépendances (réservations ou alertes)
    Given path 'products/2'
    And headers { 'Authorization': '#(adminToken)' }
    When method delete
    Then status 204

  @deleteProduct @hasVariants @error
  Scenario: Suppression non valide d'un produit ayant des variantes associées
    Given path '/products/prod_with_variants'
    And headers { 'Authorization': '#(adminToken)' }
    When method DELETE
    Then status 409
    And def expectedError = read('classpath:__files/products/responses/error_product_has_variants.json')
    And match response == expectedError

  @deleteProduct @unauthorized @error
  Scenario: Tenter de supprimer sans authentification
    Given path 'products/3'
    And header Authorization = ''
    When method delete
    Then status 401
    And match response == { "error": "Non autorisé", "message": "Un jeton d'authentification valide est requis." }

  @deleteProduct @forbidden @error
  Scenario: Tenter de supprimer un produit avec un rôle non autorisé (uniquement l'Admin siège est autorisé)
    Given path 'products/44'
    And header Authorization = 'Bearer user-no-permission'
    When method delete
    Then status 403
    And match response == { "error": "Accès interdit", "message": "Vous n'avez pas les permissions nécessaires." }

  @deleteProduct @notFound @error
  Scenario: Tenter de supprimer un produit inexistant (404 Not Found)
    Given path 'products/999'
    And header Authorization = 'Bearer valid-token'
    When method delete
    Then status 404
    And match response == { "error": "Produit non trouvé" }

  @deleteProduct @invalidId @error
  Scenario: Tenter de supprimer un produit avec un format d'ID invalide (400 Bad Request)
    Given path 'products/abc'
    And header Authorization = 'Bearer valid-token'
    When method delete
    Then status 400
    And match response == { "error": "Format d'ID invalide" }

  Scenario: Suppression d'un produit utilisé dans une commande active
    Given path 'products/4'
    And header Authorization = 'Bearer valid-token'
    When method delete
    Then status 409
    And match response == { "error": "Conflit", "message": "Produit utilisé dans une commande active." }

  Scenario: Suppression d'un produit déjà supprimé
    Given path 'products/3'
    And header Authorization = 'Bearer valid-token'
    When method delete
    Then status 204