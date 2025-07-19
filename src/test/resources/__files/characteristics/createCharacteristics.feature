@createCharacteristics @postCharacteristics
Feature: Création d'une nouvelle caracteristique (POST /characteristics)

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Content-Type': 'application/json' }

  @valid @smoke
  Scenario: Creation reussie d'une nouvelle caracteristique
    Given path 'characteristics'
    And request { "name": "Material" }
    When method POST
    Then status 201
    And match response contains { "id": "#notnull", "name": "Material" }

  @error @invalidData
  Scenario Outline: Erreurs de validation lors de la creation de caracteristique
    Given path 'characteristics'
    And request <requestBody>
    When method POST
    Then status 400
    And match response == read(<responseFile>)

    Examples:
      | requestBody      | responseFile                                                              |
      | ''               | 'classpath:__files/characteristics/mocks/post/POST_char_empty_request_response.json' |
      | '{ "name": "" }' | 'classpath:__files/characteristics/mocks/post/POST_char_empty_name_response.json'  |

  @error @conflict
  Scenario: Tenter de creer une caracteristique qui existe deja (conflit)
    Given path 'characteristics'
    And request { "name": "Color" }
    When method POST
    Then status 409
    And match response contains { "error": "Conflict" }
    And match response contains { "message": "Une caracteristique avec ce nom existe deja." }