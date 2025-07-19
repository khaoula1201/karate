@deleteStore @deleteStore
Feature: Suppression d'un magasin

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }

  @valid
  Scenario: Supprimer avec succes un magasin existant
    Given path 'stores/store-to-delete'
    When method DELETE
    Then status 204
    And match response == ''

  @notFound @error
  Scenario: Tenter de supprimer un magasin inexistant
    Given path 'stores/non-existent-store'
    When method DELETE
    Then status 404
    And match response contains { "error": "Not Found" }
    And match response contains { "message": "#string" }

  @conflict @error
  Scenario: Tenter de supprimer un magasin en utilisation
    Given path 'stores/store-in-use'
    When method DELETE
    Then status 409
    And match response contains { "error": "Conflict" }
    And match response contains { "message": "Le magasin est en cours d'utilisation." }