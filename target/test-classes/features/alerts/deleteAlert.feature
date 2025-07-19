@deleteAlert
Feature: Suppression d'une alerte par ID

  Background:
    * url 'http://localhost:8080'
    * def authHeaderAdmin = { 'Authorization': 'Bearer admin-token' }
    * def authHeaderUserNoPermission = { 'Authorization': 'Bearer user-no-permission' }

  Scenario: Tenter de supprimer une alerte existante
    Given path 'alerts/alert-12345'
    And headers authHeaderAdmin
    When method DELETE
    Then status 204

  Scenario: Tenter de supprimer une alerte inexistante
    Given path 'alerts/non-existent-alert'
    And headers authHeaderAdmin
    When method DELETE
    Then status 404
    And match response.error == 'Not Found'

  Scenario: Tenter de supprimer une alerte sans les permissions
    Given path 'alerts/alert-12345'
    And headers authHeaderUserNoPermission
    When method DELETE
    Then status 403
    And match response.error == 'Forbidden'