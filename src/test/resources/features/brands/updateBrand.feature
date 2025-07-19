@updateBrands @putBrands
Feature: Mise a jour d'une marque (PUT /brands/{id})

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }

  @valid
  Scenario: Mettre a jour avec succes une marque
    Given path 'brands/brand-1'
    And request { "id": "brand-1", "name": "Brand A - Updated" }
    When method PUT
    Then status 200
    And match response.id == 'brand-1'
    And match response.name == 'Brand A - Updated'

  @notFound @error
  Scenario: Tenter de mettre a jour une marque inexistante
    Given path 'brands/non-existent-brand-id'
    And request { "id": "non-existent-brand-id", "name": "New Name" }
    When method PUT
    Then status 404
    And match response contains { "error": "Not Found" }
    And match response contains { "message": "#string" }

  @invalidData @error
  Scenario: Mettre a jour une marque avec des donnees invalides
    Given path 'brands/brand-1'
    And request { "id": "brand-1", "name": "" }
    When method PUT
    Then status 400
    And match response contains { "error": "Bad Request" }
    And match response contains { "message": "Le nom de la marque ne peut pas etre vide." }