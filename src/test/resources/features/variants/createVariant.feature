@createVariant
Feature: Création d'une nouvelle variante de produit

  Background:
    * url 'http://localhost:8080'
    * def adminToken = 'Bearer admin-token'
    * def userNoPermissionToken = 'Bearer user-no-permission'

  @postVariant @valid
  Scenario: Création valide d'une variante de produit
    Given path 'variants'
    And headers { 'Authorization': adminToken, 'Content-Type': 'application/json' }
    And request { "product_id": "prod-456", "name": "Variante Chaussures Noires", "sku": "SKU-BLACK-SIZE42", "price": 120.00, "characteristics": [ { "id": "char-1", "value": "42" } ], "packaging": { "id": "pack-A" } }
    When method POST
    Then status 201
    And match response.id == 'variant-789'
    And match response.name == 'Variante Chaussures Noires'
    And match response.status == 'INACTIVE'
    And match response.characteristics[0].value == '42'

  @postVariant @forbidden @error
  Scenario: Tenter de créer une variante sans les permissions requises
    Given path 'variants'
    And headers { 'Authorization': userNoPermissionToken }
    And request { "product_id": "prod-456", "name": "Variant Test", "price": 100 }
    When method POST
    Then status 403
    And match response == { "error": "Accès interdit", "message": "Vous n'avez pas les permissions nécessaires pour créer une variante." }

  @postVariant @invalidData @error
  Scenario: Tenter de créer une variante avec des données invalides (caractéristiques invalides)
    Given path 'variants'
    And headers { 'Authorization': adminToken }
    And request { "product_id": "prod-456", "name": "Variant Test", "price": 100, "characteristics_invalid": true }
    When method POST
    Then status 400
    And match response == { "error": "Données de variante invalides", "message": "Les caractéristiques ou l'emballage spécifiés sont invalides." }

  @postVariant @unauthorized @error
  Scenario: Tenter de créer une variante sans en-tête Authorization
    Given path 'variants'
    And remove header Authorization
    And request { "product_id": "prod-456", "name": "Variant Test", "price": 100 }
    When method POST
    Then status 401
    And match response == { "error": "Non autorisé", "message": "Un jeton d'authentification valide est requis." }