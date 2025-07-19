@deleteBrands @deleteBrands
Feature: Suppression d'une marque (DELETE /brands/{id})

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }

  @valid
  Scenario: Supprimer avec succes une marque existante
    Given path 'brands/brand-to-delete'
    When method DELETE
    Then status 204
    And match response == ''

  @notFound @error
  Scenario: Tenter de supprimer une marque inexistante
    Given path 'brands/non-existent-brand'
    When method DELETE
    Then status 404
    And match response contains { "error": "Not Found" }
    And match response contains { "message": "#string" }

  @conflict @error
  Scenario: Tenter de supprimer une marque en utilisation
    Given path 'brands/brand-in-use'
    When method DELETE
    Then status 409
    And match response contains { "error": "Conflict" }
    And match response contains { "message": "La marque est utilisee et ne peut pas etre supprimee." }