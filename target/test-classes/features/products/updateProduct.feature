@productUpdate
Feature: Mise à jour et activation de produits
  Background:
    * url 'http://localhost:8080'
    * def adminToken = 'Bearer admin-token'
    * def userNoPermissionToken = 'Bearer user-no-permission'
    * def validToken = 'Bearer valid-token'

  Scenario Outline: Mise à jour valide d'un produit
    Given path 'products', <id>
    And header Authorization = 'Bearer valid-token'
    And request read(<requestFile>)
    When method put
    Then status <statusCode>
    And match response == read(<responseFile>)

    Examples:
      | id  | requestFile                                  | responseFile                               | statusCode |
      | 1   | 'classpath:__files/products/requests/put_product_full_update.json' | 'classpath:__files/products/responses/put_product_full_update_response.json' | 200 |
      | 1   | 'classpath:__files/products/requests/put_product_partial_update.json' | 'classpath:__files/products/responses/put_product_partial_update_response.json' | 200 |

  Scenario: Mise à jour d'un produit sans modification (données identiques)
    Given path 'products', 1
    And header Authorization = 'Bearer valid-token'
    And request read('classpath:__files/products/requests/put_product_full_update.json')
    When method put
    Then status 200
    And match response == read('classpath:__files/products/responses/put_product_full_update_response.json')

  Scenario Outline: Erreurs lors de la mise à jour d'un produit
    Given path 'products', <id>
    And header Authorization = 'Bearer valid-token'
    And request read(<requestFile>)
    When method put
    Then status <statusCode>
    And match response == read(<responseFile>)

    Examples:
      | id  | requestFile                                  | responseFile                               | statusCode |
      | 999 | 'classpath:__files/products/requests/put_product_full_update.json' | 'classpath:__files/products/responses/error_product_not_found.json' | 404 |
      | 1   | 'classpath:__files/products/requests/put_product_invalid_price.json' | 'classpath:__files/products/responses/put_product_invalid_price_response.json' | 400 |
      | 1   | 'classpath:__files/products/requests/put_product_invalid_stock.json' | 'classpath:__files/products/responses/put_product_invalid_stock_response.json' | 400 |
      | 1   | 'classpath:__files/products/requests/put_product_invalid_marque.json' | 'classpath:__files/products/responses/put_product_invalid_marque_response.json' | 400 |
      | 1   | 'classpath:__files/products/requests/put_product_invalid_emballage.json' | 'classpath:__files/products/responses/put_product_invalid_emballage_response.json' | 400 |
      | 1   | 'classpath:__files/products/requests/put_product_invalid_attribut.json' | 'classpath:__files/products/responses/put_product_invalid_attribut_response.json' | 400 |
      | 1   | 'classpath:__files/products/requests/put_product_conflict_name.json' | 'classpath:__files/products/responses/put_product_conflict_name_response.json' | 409 |

  Scenario: Mise à jour d'un produit sans authentification
    Given path 'products', 66
    And request read('classpath:__files/products/requests/put_product_full_update.json')
    And header Authorization = ''
    When method put
    Then status 401
    And match response == read('classpath:__files/common/error_unauthorized.json')