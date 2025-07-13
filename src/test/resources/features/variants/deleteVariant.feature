@deleteVariant
Feature: Suppression d'une variante de produit

  Background:
    * url 'http://localhost:8080'
    * def adminToken = 'Bearer admin-token'
    * def unauthorizedToken = 'Bearer unauthorized-token'

  @deleteVariant @valid
  Scenario: Suppression réussie d'une variante de produit
    Given path 'variants/1'
    And headers { 'Authorization': '#(adminToken)' }
    When method DELETE
    Then status 204


  @deleteVariant @forbidden @error
  Scenario: Tenter de supprimer une variante sans les permissions requises
    Given path 'variants/44'
    And header Authorization = 'Bearer user-no-permission'
    When method DELETE
    Then status 403
    And match response == { "error": "Accès interdit", "message": "Vous n'avez pas les permissions nécessaires." }
