@createStore @postStore
Feature: Création d'un nouveau magasin

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }

  @valid @smoke
  Scenario: Creation reussie d'un nouveau magasin
    Given path 'stores'
    And request read('classpath:__files/store/POST_store_valid_request.json')
    When method POST
    Then status 201
    And match response == read('classpath:__files/store/POST_store_valid_response.json')
    And match response.id == '#notnull'

  @error @invalidData
  Scenario Outline: Erreurs de validation lors de la creation de magasin
    Given path 'stores'
    And request read(<requestFile>)
    When method POST
    Then status <statusCode>
    And match response == read(<responseFile>)

    Examples:
      | requestFile                                      | responseFile                                       | statusCode |
      | 'classpath:__files/store/POST_store_empty_request.json' | 'classpath:__files/store/POST_store_empty_response.json'    | 400        |
      | 'classpath:__files/store/POST_store_missing_name_request.json' | 'classpath:__files/store/POST_store_missing_name_response.json' | 400        |

  @error @conflict
  Scenario: Tenter de creer un magasin qui existe deja (conflit)
    Given path 'stores'
    And request read('classpath:__files/store/POST_store_conflict_request.json')
    When method POST
    Then status 409
    And match response == read('classpath:__files/store/POST_store_conflict_response.json')