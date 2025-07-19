@readAlert
Feature: Lecture d'une alerte par ID

  Background:
    * url 'http://localhost:8080'
    * def authHeaderAdmin = { 'Authorization': 'Bearer admin-token' }

  Scenario: Tenter de lire une alerte existante
    Given path 'alerts/alert-12345'
    And headers authHeaderAdmin
    When method GET
    Then status 200
    And match response.id == 'alert-12345'
    And match response.type == 'STOCK_LOW'
    And match response.product_id == 'prod-456'

  Scenario: Tenter de lire une alerte inexistante
    Given path 'alerts/non-existent-alert'
    And headers authHeaderAdmin
    When method GET
    Then status 404
    And match response.error == 'Not Found'