Feature: Lecture d'un ou plusieurs produits
  Background:
    * url 'http://localhost:8080'
    * def authToken = 'Bearer any-token'

  Scenario: Consulter les détails d'un produit spécifique
    Given path '/products/prod_12345'
    When method GET
    Then status 200
    And def expectedProduct = read('classpath:__files/products/responses/get_product_id_response.json')
    And match response == expectedProduct
    And match response.createdAt == '#ignore'
    And match response.updatedAt == '#ignore'

  Scenario: récupération d'un produit avec format id invalide
    Given path '/products/abc'
    When method get
    Then status 400
    And match response contains { error: 'Invalid product ID format' }

  Scenario: Consulter un produit inexistant (404 Not Found)
    Given path '/products/nonExistentId'
    When method GET
    Then status 404
    And def expectedError = read('classpath:__files/products/responses/error_product_not_found.json')
    And match response == expectedError

  Scenario: Obtenir la liste complète des produits
    Given path '/products'
    When method GET
    Then status 200
    And match each response contains { "id": "#string", "name": "#string", price: '#number', "category_id": "#string" }

  Scenario: Rechercher un produit par nom
    Given path '/products'
    And param name = 'Ordinateur Portable'
    When method GET
    Then status 200
    And def expectedSearch = read('classpath:__files/products/responses/get_products_search_name_response.json')
    And match response == expectedSearch


  Scenario: Filtrer les produits par catégorie
    Given path '/products'
    And param category = 'CAT002'
    When method GET
    Then status 200
    And def expectedFilterCategory = read('classpath:__files/products/responses/get_products_filter_category_response.json')
    And match response == expectedFilterCategory

  Scenario: Filtrer les produits par une catégorie non-existante
    Given path '/products'
    And param categorie = 'nonexistent'
    When method get
    Then status 200
    And match response == []

  Scenario: Filtrer les produits par marque
    Given path '/products'
    And param brand = 'BRAND001'
    When method GET
    Then status 200
    And def expectedFilterBrand = read('classpath:__files/products/responses/get_products_filter_brand_response.json')
    And match response == expectedFilterBrand


