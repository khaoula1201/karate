@createAlert @postAlert
Feature: Création d'une nouvelle alerte de produit

  Background:
    * url 'http://localhost:8080'
    * def authHeaderAdmin = { 'Authorization': 'Bearer admin-token', 'Content-Type': 'application/json' }
    * def authHeaderUserNoPermission = { 'Authorization': 'Bearer user-no-permission' }
    * def authHeaderInvalid = { 'Authorization': 'Bearer invalid-token' }

  @valid
  Scenario: Création valide d'une alerte de produit
    Given path 'alerts'
    And headers authHeaderAdmin
    And request
    """
    {
      "type": "STOCK_LOW",
      "severity": "HIGH",
      "product_id": "prod-456",
      "message": "Stock faible pour le produit prod-456."
    }
    """
    When method POST
    Then status 201
    And match response.id == '#notnull'
    And match response.type == 'STOCK_LOW'
    And match response.product_id == 'prod-456'
    And match response.status == 'ACTIVE'

  @invalidData @error
  Scenario: Tenter de créer une alerte avec un corps de requête vide
    Given path 'alerts'
    And headers authHeaderAdmin
    And request {}
    When method POST
    Then status 400
    And match response.error == 'Invalid Request Body'

  @invalidData @error
  Scenario: Tenter de créer une alerte avec des données manquantes
    Given path 'alerts'
    And headers authHeaderAdmin
    And request { "type": "PRICE_CHANGE" }
    When method POST
    Then status 400
    And match response.error == 'Missing Required Fields'

  @invalidData @error
  Scenario: Tenter de créer une alerte avec un type d'alerte invalide
    Given path 'alerts'
    And headers authHeaderAdmin
    And request { "type": "INVALID_TYPE", "severity": "MEDIUM", "product_id": "prod-456", "message": "Test" }
    When method POST
    Then status 400
    And match response.error == 'Invalid Alert Type'

  @invalidAuth @error
  Scenario: Tenter de créer une alerte sans jeton d'authentification
    Given path 'alerts'
    And request { "type": "STOCK_LOW", "product_id": "prod-456" }
    When method POST
    Then status 401
    And match response.error == 'Unauthorized'

  @invalidAuth @error
  Scenario: Tenter de créer une alerte avec un jeton d'authentification invalide
    Given path 'alerts'
    And headers authHeaderInvalid
    And request { "type": "STOCK_LOW", "product_id": "prod-456" }
    When method POST
    Then status 401
    And match response.error == 'Unauthorized'

  @invalidAuth @error
  Scenario: Tenter de créer une alerte avec des permissions insuffisantes
    Given path 'alerts'
    And headers authHeaderUserNoPermission
    And request { "type": "STOCK_LOW", "product_id": "prod-456" }
    When method POST
    Then status 403
    And match response.error == 'Forbidden'