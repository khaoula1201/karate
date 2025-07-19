@readStock
Feature: Consultation et recherche de stock de produits

  Background:
    * url 'http://localhost:8080'

  @getById @valid
  Scenario: Lire les details du stock d'un produit specifique
    Given path 'stocks/stock-123'
    When method GET
    Then status 200
    And match response.id == 'stock-123'
    And match response.product_id == 'prod-456'
    And match response.quantity == 50
    And match response.location == 'WH-A'

  @getById @notFound @error
  Scenario: Tenter de lire le stock d'un produit inexistant
    Given path 'stocks/nonExistentStockId'
    When method GET
    Then status 404
    And match response == { "error": "Not Found", "message": "Le stock avec l'ID spécifié n'existe pas." }

  @getAll @valid
  Scenario: Consulter la liste complete de tous les stocks
    Given path 'stocks'
    When method GET
    Then status 200
    And match response[*].id contains 'stock-123'
    And match response[*].id contains 'stock-456'
    And match response[*].id contains 'stock-789'

  @searchByProductId @filter
  Scenario: Rechercher un stock par ID de produit
    Given path 'stocks'
    And param product_id = 'prod-101'
    When method GET
    Then status 200
    And match response[0].product_id == 'prod-101'

  @filterByLocation @filter
  Scenario: Filtrer les stocks par localisation (WH-B)
    Given path 'stocks'
    And param location = 'WH-B'
    When method GET
    Then status 200
    And match each response[*].location == 'WH-B'


  @filterByQuantity @filter
  Scenario: Filtrer les stocks dont la quantite est superieure a 20
    Given path 'stocks'
    And param quantity_gt = 20
    When method GET
    Then status 200
    And match each response contains { quantity: '#number? _ > 20' }

  @combineFilters @filter
  Scenario: Combiner les filtres de recherche (location et product_id)
    Given path 'stocks'
    And param product_id = 'prod-456'
    And param location = 'WH-A'
    When method GET
    Then status 200
    And match response[0].product_id == 'prod-456'
    And match response[0].location == 'WH-A'