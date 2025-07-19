@uploadImage @postImage
Feature: Téléchargement d'une image (POST /images)

  Background:
    * url 'http://localhost:8080'
    * path 'images'

  @valid @smoke
  Scenario: Telechargement reussi d'une image
    Given multipart file image = read('classpath:__files/images/test.jpg')
    When method POST
    Then status 201
    And match response contains { "id": "#notnull", "url": "#string" }

  @invalidFile @error
  Scenario: Tenter de telecharger un fichier non-image
    Given multipart file image = read('classpath:__files/images/test.txt')
    When method POST
    Then status 400
    And match response contains { "error": "Bad Request", "message": "Le type de fichier n'est pas une image valide." }

  @invalidRequest @error
  Scenario: Tenter un telechargement avec un corps de requete invalide
    Given request { "invalid_key": "some_value" }
    When method POST
    Then status 400
    And match response contains { "error": "Bad Request", "message": "Le corps de la requete doit etre multipart/form-data et contenir un fichier." }