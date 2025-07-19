@createVariant
Feature: Création d'une nouvelle variante de produit

  Background:
  Background:
    * url 'http://localhost:8080'
    * def adminToken = 'Bearer admin-token'

  Scenario: Création valide d'une variante de produit
    Given path 'variants'
    And headers { 'Content-Type': 'application/json', 'Authorization': '#(adminToken)' }
    And request { "product_id": "prod-456", "name": "Variante Chaussures Noires", "sku": "SKU-BLACK-SIZE42", "price": 120.00, "characteristics": [ { "id": "char-1", "value": "42" } ], "packaging": { "id": "pack-A" } }
    When method POST
    Then status 201
    And match response == read('classpath:__files/variants/responses/post_variant_success.json')

  Scenario: Tenter de créer une variante sans les permissions requises
    Given path 'variants'
    And headers { 'Content-Type': 'application/json' }
    And request { "product_id": "prod-456", "name": "Variant Test", "price": 100 }
    When method POST
    Then status 401
    And def expectedErrorResponse = read('classpath:__files/common/error_unauthorized.json')
    And match response == expectedErrorResponse

  Scenario: Tenter de créer une variante avec des données invalides (caractéristiques invalides)
    Given path 'variants'
    And headers { 'Content-Type': 'application/json', 'Authorization': '#(adminToken)' }
    And request { "product_id": "prod-456", "name": "Variant Test", "price": 100, "characteristics_invalid": true }
    When method POST
    Then status 400
    And match response == { "error": "Données de variante invalides", "message": "Les caractéristiques ou l'emballage spécifiés sont invalides." }

