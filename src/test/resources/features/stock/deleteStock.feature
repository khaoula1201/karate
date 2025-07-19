@deleteStock @high
Feature: Suppression d'un stock

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }
    * def adminToken = 'Bearer admin-token'

  @valid
  Scenario: Supprimer avec succes un stock existant
    Given path 'stocks/stock-to-delete-123'
    And headers { 'Authorization': adminToken }
    When method DELETE
    Then status 204
    And match response == ''
  @notFound @error
  Scenario: Tenter de supprimer un stock inexistant
    Given path 'stocks/non-existent-stock-id'
    And headers { 'Authorization': adminToken }
    When method DELETE
    Then status 404
    And match response contains { "error": "Not Found" }
    And match response contains { "message": "#string" }

  @conflict @error
  Scenario: Tenter de supprimer un stock utilise ou reserve
    Given path 'stocks/stock-in-use-456'
    And headers { 'Authorization': adminToken }
    When method DELETE
    Then status 409
    And match response contains { "error": "Conflict" }
    And match response contains { "message": "Le stock est actuellement utilise et ne peut pas etre supprime." }