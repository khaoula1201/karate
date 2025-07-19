@readVariant
Feature: Consultation et recherche de variantes de produits

  Background:
    * url 'http://localhost:8080'
    * configure headers = { 'Accept': 'application/json' }

  @getVariant @valid
  Scenario: Lire les détails d'une variante spécifique
    Given path 'variants/variant-123'
    When method GET
    Then status 200
    And match response.id == 'variant-123'
    And match response.name == 'Variante Chaussures Rouges'
    And match response.status == 'ACTIVE'

  @getVariant @notFound @error
  Scenario: Tenter de lire une variante inexistante
    Given path 'variants/nonExistentVariantId'
    When method GET
    Then status 404
    And match response == { "error": "Variante non trouvée", "message": "La variante avec l'ID spécifié n'existe pas." }


  Scenario: Consulter la liste complète de toutes les variantes
    Given path 'variants'
    When method GET
    Then status 200
    * print response
    And match response contains { id: 'variant-123', name: 'Variante Chaussures Rouges' }
    And match response contains { id: 'variant-789', name: 'Variante Chaussures Noires' }
    And match response contains { id: 'variant-987', name: 'Variante Sac à main en cuir' }

  @getVariant @search @filter
  Scenario: Rechercher une variante par nom
    Given path 'variants'
    And param name = 'Variante Rouge'
    When method GET
    Then status 200
    And match response[0].name == 'Variante Chaussures Rouges'


  Scenario: Filtrer les variantes par produit principal
    Given path 'variants'
    And param product = 'prod-456'
    When method GET
    Then status 200
    * print 'Response:', response
    And match each response[*].product_id == 'prod-456'