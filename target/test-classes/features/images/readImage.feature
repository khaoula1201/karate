@readImage @getImage
Feature: Lecture d'une image (GET /images/{id})

  Background:
    * url 'http://localhost:8080'

  @valid
  Scenario: Lire le contenu d'une image existante
    Given path 'images/image-123'
    When method GET
    Then status 200
    And match responseHeaders['Content-Type'] contains 'image/jpeg'

  @notFound @error
  Scenario: Tenter de lire une image inexistante
    Given path 'images/non-existent-image-id'
    When method GET
    Then status 404
    And match response contains { "error": "Not Found", "message": "L'image n'a pas ete trouvee." }