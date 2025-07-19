@deleteImage @deleteImage
Feature: Suppression d'une image (DELETE /images/{id})

  Background:
    * url 'http://localhost:8080'

  @valid
  Scenario: Supprimer avec succes une image existante
    Given path 'images/image-to-delete-456'
    When method DELETE
    Then status 204
    And match response == ''

  @notFound @error
  Scenario: Tenter de supprimer une image inexistante
    Given path 'images/non-existent-image'
    When method DELETE
    Then status 404
    And match response contains { "error": "Not Found", "message": "L'image n'a pas ete trouvee." }