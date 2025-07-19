@updateAlert
Feature: Mise à jour d'une alerte par ID

  Background:
    * url 'http://localhost:8080'
    * def authHeaderAdmin = { 'Authorization': 'Bearer admin-token', 'Content-Type': 'application/json' }
    * def authHeaderUserNoPermission = { 'Authorization': 'Bearer user-no-permission', 'Content-Type': 'application/json' }

  Scenario: Tenter de mettre à jour le statut d'une alerte existante
    Given path 'alerts/alert-12345'
    And headers authHeaderAdmin
    And request { "status": "INACTIVE" }
    When method PUT
    Then status 200
    And match response.id == 'alert-12345'
    And match response.status == 'INACTIVE'

  Scenario: Tenter de mettre à jour une alerte inexistante
    Given path 'alerts/non-existent-alert'
    And headers authHeaderAdmin
    And request { "status": "INACTIVE" }
    When method PUT
    Then status 404
    And match response.error == 'Not Found'

  Scenario: Tenter de mettre à jour une alerte avec un corps vide
    Given path 'alerts/alert-12345'
    And headers authHeaderAdmin
    And request {}
    When method PUT
    Then status 400
    And match response.error == 'Bad Request'

  Scenario: Tenter de mettre à jour une alerte sans les permissions
    Given path 'alerts/alert-12345'
    And headers authHeaderUserNoPermission
    And request { "status": "INACTIVE" }
    When method PUT
    Then status 403
    And match response.error == 'Forbidden'