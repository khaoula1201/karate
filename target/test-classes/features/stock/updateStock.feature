@updateStock @putStock
Feature: Mise a jour d'un stock

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }
    * def adminToken = 'Bearer admin-token'

  @valid
  Scenario: Mettre a jour avec succes le stock d'un produit
    Given path 'stocks/stock-123'
    And headers { 'Authorization': adminToken }
    And request { "id": "stock-123", "quantity": 75, "location": "WH-A" }
    When method PUT
    Then status 200
    And match response.id == 'stock-123'
    And match response.quantity == 75
    And match response.location == 'WH-A'

  @notFound @error
  Scenario: Tenter de mettre a jour un stock inexistant
    Given path 'stocks/non-existent-id'
    And headers { 'Authorization': adminToken }
    And request { "id": "non-existent-id", "quantity": 100, "location": "WH-C" }
    When method PUT
    Then status 404
    And match response contains { "error": "Not Found" }
    And match response contains { "message": "#string" }

  @invalidData @error
  Scenario: Mettre a jour un stock avec des donnees invalides (quantite negative)
    Given path 'stocks/stock-123'
    And headers { 'Authorization': adminToken }
    And request { "id": "stock-123", "quantity": -10, "location": "WH-A" }
    When method PUT
    Then status 400
    And match response contains { "error": "Bad Request" }
    And match response contains { "message": "La quantite doit etre un nombre positif." }