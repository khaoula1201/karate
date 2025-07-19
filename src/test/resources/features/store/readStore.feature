@readStore @getStore
Feature: Consultation des magasins

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }

  @getAll @valid
  Scenario: Lire la liste complete de tous les magasins
    Given path 'stores'
    When method GET
    Then status 200
    And match response contains any { id: 'store-1', name: 'Store A', location: 'Paris' }
    And match response contains any { id: 'store-2', name: 'Store B', location: 'Lyon' }

  @getById @valid
  Scenario: Lire les details d'un magasin specifique
    Given path 'stores/store-1'
    When method GET
    Then status 200
    And match response.id == 'store-1'
    And match response.name == 'Store A'
    And match response.location == 'Paris'

  @getById @notFound @error
  Scenario: Tenter de lire un magasin inexistant
    Given path 'stores/non-existent-store-id'
    When method GET
    Then status 404
    And match response contains { "error": "Not Found" }
    And match response contains { "message": "#string" }