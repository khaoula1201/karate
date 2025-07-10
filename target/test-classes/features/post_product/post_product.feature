Feature:API Product - POST Endpoints

  Background:
    * url 'http://localhost:8080'
    * header Content-Type = 'application/json'

  Scenario Outline: Création valide d'un produit
    Given path <endpoint>
    And header Authorization = 'Bearer valid-token'
    And request read(<requestFile>)
    When method post
    Then status <statusCode>
    And match response == read(<responseFile>)

    Examples:
      | endpoint   | requestFile                                  | responseFile                               | statusCode |
      | 'products' | 'classpath:__files/product/mocks/post/POST_product_base.json' | 'classpath:__files/product/mocks/post/POST_product_response_success.json' | 201 |