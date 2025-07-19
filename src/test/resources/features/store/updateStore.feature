@updateStore @putStore
Feature: Mise a jour d'un magasin

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }

  @valid
  Scenario: Mettre a jour avec succes un magasin
    Given path 'stores/store-1'
    And request { "id": "store-1", "name": "Store A - Updated", "location": "Paris" }
    When method PUT
    Then status 200
    And match response.id == 'store-1'
    And match response.name == 'Store A - Updated'

  @notFound @error
  Scenario: Tenter de mettre a jour un magasin inexistant
    Given path 'stores/non-existent-store-id'
    And request { "id": "non-existent-store-id", "name": "New Name", "location": "New Location" }
    When method PUT
    Then status 404
    And match response contains { "error": "Not Found" }
    And match response contains { "message": "#string" }

  @invalidData @error
  Scenario: Mettre a jour un magasin avec des donnees invalides
    Given path 'stores/store-1'
    And request { "id": "store-1", "name": "", "location": "Paris" }
    When method PUT
    Then status 400
    And match response contains { "error": "Bad Request" }
    And match response contains { "message": "Le nom du magasin ne peut pas etre vide." }