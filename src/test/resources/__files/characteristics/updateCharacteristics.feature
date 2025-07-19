@updateCharacteristics @putCharacteristics
Feature: Mise a jour d'une caracteristique (PUT /characteristics/{id})

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }

  @valid
  Scenario: Mettre a jour avec succes une caracteristique
    Given path 'characteristics/char-1'
    And request { "id": "char-1", "name": "Color - Updated" }
    When method PUT
    Then status 200
    And match response.id == 'char-1'
    And match response.name == 'Color - Updated'

  @notFound @error
  Scenario: Tenter de mettre a jour une caracteristique inexistante
    Given path 'characteristics/non-existent-char-id'
    And request { "id": "non-existent-char-id", "name": "New Name" }
    When method PUT
    Then status 404
    And match response contains { "error": "Not Found" }
    And match response contains { "message": "#string" }

  @invalidData @error
  Scenario: Mettre a jour une caracteristique avec des donnees invalides
    Given path 'characteristics/char-1'
    And request { "id": "char-1", "name": "" }
    When method PUT
    Then status 400
    And match response contains { "error": "Bad Request" }
    And match response contains { "message": "Le nom de la caracteristique ne peut pas etre vide." }