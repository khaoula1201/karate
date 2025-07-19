@createBrands @postBrands
Feature: Création d'une nouvelle marque (POST /brands)

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }

  @valid @smoke
  Scenario: Creation reussie d'une nouvelle marque
    Given path 'brands'
    And request { "name": "New Brand" }
    When method POST
    Then status 201
    And match response contains { "id": "#notnull", "name": "New Brand" }

  @error @invalidData
  Scenario Outline: Erreurs de validation lors de la creation de marque
    Given path 'brands'
    And request read(<requestFile>)
    When method POST
    Then status 400
    And match response == read(<responseFile>)

    Examples:
      | requestFile                                           | responseFile                                       |
      | 'classpath:__files/brands/POST_brand_empty_request.json' | 'classpath:__files/brands/POST_brand_empty_request_response.json' |
      | 'classpath:__files/brands/POST_brand_empty_name_request.json' | 'classpath:__files/brands/POST_brand_empty_name_response.json' |

  @error @conflict
  Scenario: Tenter de creer une marque qui existe deja (conflit)
    Given path 'brands'
    And request { "name": "Brand A" }
    When method POST
    Then status 409
    And match response contains { "error": "Conflict" }
    And match response contains { "message": "Une marque avec ce nom existe deja." }