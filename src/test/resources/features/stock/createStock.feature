@createStock @postStock
Feature: Création d'un nouveau stock de produit

  Background:
    * url 'http://localhost:8080'
    * def authHeaderAdmin = { 'Authorization': 'Bearer admin-token', 'Content-Type': 'application/json' }
    * def authHeaderUserNoPermission = { 'Authorization': 'Bearer user-no-permission', 'Content-Type': 'application/json' }
    * def authHeaderInvalid = { 'Authorization': 'Bearer invalid-token', 'Content-Type': 'application/json' }

  @valid @smoke
  Scenario: Création reussie d'un nouveau stock
    Given path 'stocks'
    And headers authHeaderAdmin
    And request read('classpath:__files/stock/POST_stock_valid_request.json')
    When method POST
    Then status 201
    And match response == read('classpath:__files/stock/POST_stock_valid_response.json')
    And match response.id == '#notnull'

  @error @invalidData
  Scenario Outline: Erreurs de validation lors de la creation de stock
    Given path 'stocks'
    And headers authHeaderAdmin
    And request read(<requestFile>)
    When method POST
    Then status <statusCode>
    And match response == read(<responseFile>)

    Examples:
      | requestFile                                           | responseFile                                            | statusCode |
      | 'classpath:__files/stock/POST_stock_empty_request.json'         | 'classpath:__files/stock/POST_stock_empty_response.json'          | 400        |
      | 'classpath:__files/stock/POST_stock_missing_product_id_request.json' | 'classpath:__files/stock/POST_stock_missing_product_id_response.json' | 400        |
      | 'classpath:__files/stock/POST_stock_invalid_quantity_type_request.json' | 'classpath:__files/stock/POST_stock_invalid_quantity_type_response.json' | 400        |
      | 'classpath:__files/stock/POST_stock_negative_quantity_request.json' | 'classpath:__files/stock/POST_stock_negative_quantity_response.json' | 400        |

  @error @conflict
  Scenario: Tenter de creer un stock qui existe deja (conflit)
    Given path 'stocks'
    And headers authHeaderAdmin
    And request read('classpath:__files/stock/POST_stock_conflict_request.json')
    When method POST
    Then status 409
    And match response == read('classpath:__files/stock/POST_stock_conflict_response.json')

  @error @security @unauthorized
  Scenario: Tenter de creer un stock sans jeton d'authentification
    Given path 'stocks'
    And remove header Authorization
    And headers { 'Content-Type': 'application/json' }
    And request read('classpath:__files/stock/POST_stock_valid_request.json')
    When method POST
    Then status 401
    And match response == read('classpath:__files/stock/POST_stock_unauthorized_response.json')

  @error @security @invalidToken
  Scenario: Tenter de creer un stock avec un jeton d'authentification invalide
    Given path 'stocks'
    And headers authHeaderInvalid
    And request read('classpath:__files/stock/POST_stock_valid_request.json')
    When method POST
    Then status 401
    And match response == read('classpath:__files/stock/POST_stock_invalid_token_response.json')

  @error @security @forbidden
  Scenario: Tenter de creer un stock avec des permissions insuffisantes
    Given path 'stocks'
    And headers authHeaderUserNoPermission
    And request read('classpath:__files/stock/POST_stock_valid_request.json')
    When method POST
    Then status 403
    And match response == read('classpath:__files/stock/POST_stock_forbidden_response.json')