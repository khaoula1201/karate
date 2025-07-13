@putVariant
Feature: Mise à jour et activation d'une variante de produit

  Background:
    * url 'http://localhost:8080'
    * def authHeaderAdmin = { 'Authorization': 'Bearer admin-token', 'Content-Type': 'application/json' }
    * def authHeaderUserNoPermission = { 'Authorization': 'Bearer user-no-permission' }

  @putVariant @valid
  Scenario: Mise à jour réussie des informations d'une variante
    Given path 'variants/variant-123'
    And headers authHeaderAdmin
    And request { "price": 130.00, "packaging": { "id": "pack-B" }, "characteristics": [ { "id": "char-2", "value": "Rouge Pailleté" } ] }
    When method PUT
    Then status 200
    And match response.price == 130.00
    And match response.packaging.name == 'Emballage Premium'
    And match response.characteristics[1].value == 'Rouge Pailleté'

  @putVariant @forbidden @error
  Scenario: Tenter de mettre à jour une variante sans les permissions requises
    Given path 'variants/any-variant-id'
    And headers authHeaderUserNoPermission
    And request { "price": 10.00 }
    When method PUT
    Then status 403
    And match response == { "error": "Accès interdit", "message": "Vous n'avez pas les permissions nécessaires." }

  @putVariant @price @valid
  Scenario: Définir un prix unitaire pour une variante
    Given path 'variants/variant-123/price'
    And headers authHeaderAdmin
    And request { "price": 50.00 }
    When method PUT
    Then status 200
    And match response.price == 50.00

  @putVariant @activate @valid
  Scenario: Activation réussie d'une variante
    Given path 'variants/variant-to-activate/activate'
    And headers authHeaderAdmin
    When method PUT
    Then status 200
    And match response.status == 'ACTIVE'
    And match response.message == 'Variante activée avec succès.'