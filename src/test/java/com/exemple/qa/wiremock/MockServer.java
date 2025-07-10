package com.exemple.qa.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public class MockServer {
    private static WireMockServer wireMock; //static car on aura 1 seul pr tte lapp
    public static final int MOCK_PORT = 8080;

    public static void start() {
        if (wireMock == null) {
            wireMock = new WireMockServer(MOCK_PORT);
            FileSource filesFileSource = new SingleRootFileSource("src/test/resources");
            wireMock.enableRecordMappings(filesFileSource, filesFileSource);
            wireMock.start();
            configureProductStubs(wireMock);
            System.out.println("WireMock démarré sur le port : http://localhost:" + MOCK_PORT);
        }
    }
    public static void stop() {
        if (wireMock != null && wireMock.isRunning()) {
            wireMock.stop();
            System.out.println("WireMock arrêté.");
            wireMock = null;
        }
    }
    private static void configureProductStubs(WireMockServer server) {
        // Créer un nouveau produit de base avec des données valides
        server.stubFor(post(urlEqualTo("/products"))
                .atPriority(6)
                .withHeader("Content-Type", containing("application/json"))
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("products/responses/post_product_response_success.json")));

        //créer produit sans autorisation
        server.stubFor(post(urlEqualTo("/products"))
                .atPriority(5)
                .withHeader("Authorization", notMatching("Bearer .+"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withBodyFile("common/error_unauthorized.json")));

        // creer produit with Invalid Category/Brand
        server.stubFor(post(urlEqualTo("/products"))
                .atPriority(1)
                .withHeader("Content-Type", containing("application/json"))
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .withRequestBody(or(
                        matchingJsonPath("$.category_id", equalTo("INVALID_CAT_ID")),
                        matchingJsonPath("$.brand_id", equalTo("INVALID_BRAND_ID"))
                ))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("products/responses/error_invalid_category_brand.json")));

        // post avecChamp obligatoire manquant
        server.stubFor(post(urlEqualTo("/products"))
                .atPriority(2)
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(matchingJsonPath("$[?(@.name == null || !@.name)]"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBodyFile("products/responses/post_product_missing_name_response.json")));

        // post avec Prix invalide
        server.stubFor(post(urlEqualTo("/products"))
                .atPriority(3)
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(matchingJsonPath("$.price", containing("-")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBodyFile("products/responses/post_product_invalid_price_response.json")));

           //post Produit déjà existant (simulé avec un nom spécifique)
          server.stubFor(post(urlEqualTo("/products"))
                .atPriority(4)
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalToJson("{ \"name\": \"Smartphone XYZ\" }", true, true))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withBodyFile("products/responses/post_product_conflict_response.json")));

        //  GET /products/{id} - Produit non trouvé (404)
        server.stubFor(get(urlPathEqualTo("/products/nonExistentId"))
                .atPriority(1)
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("products/responses/error_product_not_found.json")));
        // GET /products/{id} - Consulter les détails d'un produit spécifique (Succès)
        server.stubFor(get(urlEqualTo("/products/prod_12345"))
                        .willReturn(aResponse()
                                .withStatus(200)
                             .withBodyFile("products/responses/get_product_id_response.json")));
        server.stubFor(get(urlEqualTo("/products/abc"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("{ \"error\": \"Invalid product ID format\" }")));



        // 3. GET /products?name={nom} - Rechercher un produit par nom
        server.stubFor(get(urlPathEqualTo("/products"))
                .withQueryParam("name", equalTo("Ordinateur Portable"))
                .atPriority(3)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("products/responses/get_products_search_name_response.json")));
        // 4. GET /products?category={id} - Filtrer les produits par catégorie
        server.stubFor(get(urlPathEqualTo("/products"))
                .withQueryParam("category", equalTo("CAT002"))
                .atPriority(4)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("products/responses/get_products_filter_category_response.json")));
        //cat non existante
        server.stubFor(get(urlPathEqualTo("/products"))
                .withQueryParam("categorie", equalTo("nonexistent"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("[]")));
        // 5. GET /products?brand={id} - Filtrer les produits par marque
        server.stubFor(get(urlPathEqualTo("/products"))
                .withQueryParam("brand", equalTo("BRAND001"))
                .atPriority(5)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("products/responses/get_products_filter_brand_response.json")));

        // 6. GET /products - Obtenir la liste complète des produits (le plus général)
        server.stubFor(get(urlEqualTo("/products"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("products/responses/get_products_list_response.json")));


    }




}