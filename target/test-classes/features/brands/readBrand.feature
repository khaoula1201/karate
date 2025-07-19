@readBrands @getBrands
Feature: Consultation des marques

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }

  @getAll @valid
  Scenario: Lire la liste complete de toutes les marques
    Given path 'brands'
    When method GET
    Then status 200
    And match response contains any { id: 'brand-1', name: 'Brand A' }
    And match response contains any { id: 'brand-2', name: 'Brand B' }

  @getById @valid
  Scenario: Lire les details d'une marque specifique
    Given path 'brands/brand-1'
    When method GET
    Then status 200
    And match response.id == 'brand-1'
    And match response.name == 'Brand A'

  @getById @notFound @error
  Scenario: Tenter de lire une marque inexistante
    Given path 'brands/non-existent-brand-id'
    When method GET
    Then status 404
    And match response contains { "error": "Not Found" }
    And match response contains { "message": "#string" }