@readCharacteristics @getCharacteristics
Feature: Consultation des caracteristiques

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }

  @getAll @valid
  Scenario: Lire la liste complete de toutes les caracteristiques
    Given path 'characteristics'
    When method GET
    Then status 200
    And match response.length == 2
    And match response contains any { id: 'char-1', name: 'Color' }
    And match response contains any { id: 'char-2', name: 'Size' }

  @getById @valid
  Scenario: Lire les details d'une caracteristique specifique
    Given path 'characteristics/char-1'
    When method GET
    Then status 200
    And match response.id == 'char-1'
    And match response.name == 'Color'

  @getById @notFound @error
  Scenario: Tenter de lire une caracteristique inexistante
    Given path 'characteristics/non-existent-char-id'
    When method GET
    Then status 404
    And match response contains { "error": "Not Found" }
    And match response contains { "message": "#string" }