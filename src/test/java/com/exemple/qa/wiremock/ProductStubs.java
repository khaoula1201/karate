package com.exemple.qa.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class ProductStubs {

    public static void configure(WireMockServer server) {

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
// DELETE STUBS (lowest number = highest priority)

        // DELETE 0: User sans permissions (403 Forbidden)
        server.stubFor(delete(urlPathMatching("/products/.*"))
                .atPriority(0)
                .withHeader("Authorization", equalTo("Bearer user-no-permission"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withBody("{\"error\":\"Accès interdit\",\"message\":\"Vous n'avez pas les permissions nécessaires.\"}")));

        // DELETE 1: ID invalide (format) - pour "abc"
        server.stubFor(delete(urlEqualTo("/products/abc"))
                .atPriority(1)
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("{\"error\":\"Format d'ID invalide\"}")));

        // DELETE 2: Suppression d'un produit ayant des variantes associées (409 Conflict)
        server.stubFor(delete(urlPathEqualTo("/products/prod_with_variants"))
                .atPriority(2)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("products/responses/error_product_has_variants.json")));

        // DELETE 3: Produit inexistant (404 Not Found) - pour "999"
        server.stubFor(delete(urlEqualTo("/products/999"))
                .atPriority(3)
                .withHeader("Authorization", equalTo("Bearer valid-token"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("{\"error\":\"Produit non trouvé\"}")));

        // DELETE 4: Produit utilisé dans une commande active (409 Conflict) - pour "4"
        server.stubFor(delete(urlEqualTo("/products/4"))
                .atPriority(4)
                .withHeader("Authorization", equalTo("Bearer valid-token"))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withBody("{\"error\":\"Conflit\",\"message\":\"Produit utilisé dans une commande active.\"}")));

        // DELETE 5: Sans en-tête Authorization / Jeton invalide (401 Unauthorized)
        // DELETE 5a: 401 Unauthorized - Jeton présent mais invalide/non autorisé
        server.stubFor(delete(urlPathMatching("/products/.*")) // S'applique à n'importe quel ID
                .atPriority(5)
                .withHeader("Authorization", matching("Bearer .+"))
                .withHeader("Authorization", notMatching("^(Bearer (admin-token|user-no-permission|valid-token))$"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Non autorisé\",\"message\":\"Un jeton d'authentification valide est requis.\"}")));

        // DELETE 5b: 401 Unauthorized - Header Authorization manquant
        server.stubFor(delete(urlPathMatching("/products/.*"))
                .atPriority(6)
                .withHeader("Authorization", absent())
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Non autorisé\",\"message\":\"Un jeton d'authentification valide est requis.\"}")));

        // DELETE 6: Suppression valide d'un produit de base - pour "1"
        server.stubFor(delete(urlEqualTo("/products/1"))
                .atPriority(6)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .willReturn(aResponse()
                        .withStatus(204)));

        // DELETE 7: Suppression valide d'un produit avec des dépendances
        server.stubFor(delete(urlEqualTo("/products/2"))
                .atPriority(7)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .willReturn(aResponse()
                        .withStatus(204)));

        // DELETE 8: Produit déjà supprimé (ou suppression idempotente)
        server.stubFor(delete(urlEqualTo("/products/3"))
                .atPriority(8)
                .withHeader("Authorization", equalTo("Bearer valid-token"))
                .willReturn(aResponse()
                        .withStatus(204)));

        //PUT
        server.stubFor(put(urlEqualTo("/products/66"))
                .atPriority(49)
                .withHeader("Authorization", matching("Bearer .+"))
                .withHeader("Authorization", notMatching("^(Bearer (admin-token|user-no-permission|valid-token))$"))                .willReturn(aResponse()
                        .withStatus(401)
                        .withBodyFile("common/error_unauthorized.json")));
        // 2.1 Produit inexistant
        server.stubFor(put(urlPathMatching("/products/999"))
                .atPriority(80)
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBodyFile("products/responses/error_product_not_found.json")));
        // 2.2 Prix négatif
        server.stubFor(put(urlEqualTo("/products/1"))
                .atPriority(81)
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(matchingJsonPath("$.prixDeBase", containing("-")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBodyFile("products/responses/put_product_invalid_price_response.json")));
        // 2.3 Stock négatif
        server.stubFor(put(urlEqualTo("/products/1"))
                .atPriority(82)
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(matchingJsonPath("$.stock_reel", containing("-")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBodyFile("products/responses/put_product_invalid_stock_response.json")));
        // 2.4 Marque inexistant
        server.stubFor(put(urlEqualTo("/products/1"))
                .atPriority(83)
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalToJson("{ \"marqueId\": 999 }", true, true))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBodyFile("products/responses/put_product_invalid_marque_response.json")));
        // 2.5 Emballage inexistant
        server.stubFor(put(urlEqualTo("/products/1"))
                .atPriority(84)
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalToJson("{ \"emballageId\": 999 }", true, true))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBodyFile("products/responses/put_product_invalid_emballage_response.json")));
        // 2.6 Attribut inexistant
        server.stubFor(put(urlEqualTo("/products/1"))
                .atPriority(85)
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(matchingJsonPath("$.attributs[?(@.id == 999)]"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBodyFile("products/responses/put_product_invalid_attribut_response.json")));
        // 2.8 Conflit d'unicité (nom)
        server.stubFor(put(urlEqualTo("/products/1"))
                .atPriority(87)
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalToJson("{ \"nom\": \"Smartphone ABC\" }", true, true))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withBodyFile("products/responses/put_product_conflict_name_response.json")));


        // 3.1 Mise à jour complète
        server.stubFor(put(urlEqualTo("/products/1"))
                .atPriority(100)
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalToJson(readFileFromResources("__files/products/requests/put_product_full_update.json"), true, true))                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("products/responses/put_product_full_update_response.json")));
// 3.2 Mise à jour partielle
        server.stubFor(put(urlEqualTo("/products/1"))
                .atPriority(101)
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalToJson(readFileFromResources("__files/products/requests/put_product_partial_update.json"), true, true))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("products/responses/put_product_partial_update_response.json")));

        // 3.4 Mise à jour sans modification (données identiques)

        server.stubFor(put(urlEqualTo("/products/1"))
                .atPriority(104)
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("products/responses/put_product_full_update_response.json")));




    }
    private static String readFileFromResources(String path) {
        try {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
            if (resource == null) {
                throw new IllegalArgumentException("Fichier non trouvé : " + path);
            }
            return Files.readString(Paths.get(resource.toURI()));
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier : " + path, e);
        }
    }



}