@deleteCharacteristics @deleteCharacteristics
Feature: Suppression d'une caracteristique (DELETE /characteristics/{id})

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }

  @valid
  Scenario: Supprimer avec succes une caracteristique existante
    Given path 'characteristics/char-to-delete'
    When method DELETE
    Then status 204
    And match response == ''

  @notFound @error
  Scenario: Tenter de supprimer une caracteristique inexistante
    Given path 'characteristics/non-existent-char'
    When method DELETE
    Then status 404
    And match response contains { "error": "Not Found" }
    And match response contains { "message": "#string" }

  @conflict @error
  Scenario: Tenter de supprimer une caracteristique en utilisation
    Given path 'characteristics/char-in-use'
    When method DELETE
    Then status 409
    And match response contains { "error": "Conflict" }
    And match response contains { "message": "La caracteristique est utilisee et ne peut pas etre supprimee." }