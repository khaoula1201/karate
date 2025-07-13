@productCreation
Feature: Création et validation d'un produit principal
  Background:
    * url 'http://localhost:8080'
    * def adminToken = 'Bearer admin-token'

  @createProduct @valid
  Scenario: Créer un produit de base valide (Admin Siege)
    Given path '/products'
    And request read('classpath:__files/products/requests/post_product_base.json')
    And headers { 'Content-Type': 'application/json', 'Authorization': '#(adminToken)' }
    When method POST
    Then status 201
    And match response == read('classpath:__files/products/responses/post_product_response_success.json')

  @createProduct @unauthorized @error
  Scenario: Tenter de créer un produit sans les autorisations nécessaires
    Given path '/products'
    And headers { 'Content-Type': 'application/json' }
    And request { "name": "Unauthorized Product", "price": 100, "category_id": "cat_test", "brand_id": "brand_test" }
    When method POST
    Then status 401
    And def expectedErrorResponse = read('classpath:__files/common/error_unauthorized.json')
    And match response == expectedErrorResponse

  @createProduct @invalidData @error
  Scenario: Tenter de créer un produit avec une catégorie ou marque inexistante
    Given path '/products'
    And headers { 'Content-Type': 'application/json', 'Authorization': '#(adminToken)' }
    And request { "name": "Product with Invalid Category", "price": 200, "category_id": "INVALID_CAT_ID", "brand_id": "BRAND001" }
    When method POST
    Then status 400
    And def expectedInvalidIdError = read('classpath:__files/products/responses/error_invalid_category_brand.json')
    And match response == expectedInvalidIdError

  @createProduct @validation @error
  Scenario Outline: Erreurs lors de la création d'un produit
    Given path <endpoint>
    And request read(<requestFile>)
    When method post
    Then status <statusCode>
    And match response == read(<responseFile>)

    Examples:
      | endpoint   | requestFile                                  | responseFile                               | statusCode |
      | 'products' | 'classpath:__files/products/requests/post_product_missing_name.json' | 'classpath:__files/products/responses/post_product_missing_name_response.json' | 400 |
      | 'products' | 'classpath:__files/products/requests/post_product_invalid_price.json' | 'classpath:__files/products/responses/post_product_invalid_price_response.json' | 400 |
      | 'products' | 'classpath:__files/products/requests/post_product_conflict.json' | 'classpath:__files/products/responses/post_product_conflict_response.json' | 409 |