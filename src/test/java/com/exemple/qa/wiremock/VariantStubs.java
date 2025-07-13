package com.exemple.qa.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class VariantStubs {

    public static void configure(WireMockServer server) {
        configurePostStubs(server);
        configurePutStubs(server);
        configureDeleteStubs(server);
        configureGetStubs(server);
    }

    private static void configurePostStubs(WireMockServer server) {
        // PRIORITÉ 1 : Tenter de créer une variante avec des données invalides (400 BAD REQUEST)
        // C'est le plus spécifique, il doit être testé en premier.
        server.stubFor(post(urlEqualTo("/variants"))
                .atPriority(1)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .withRequestBody(matchingJsonPath("$.packaging_id", equalTo("INVALID_PACK_ID")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("{\"error\":\"Données de variante invalides\",\"message\":\"Les caractéristiques ou l'emballage spécifiés sont invalides.\"}")));

        // PRIORITÉ 2 : Tenter de créer une variante sans les autorisations nécessaires (403 FORBIDDEN)
        // Matcher sur l'URL et un en-tête d'autorisation précis.
        server.stubFor(post(urlEqualTo("/variants"))
                .atPriority(2)
                .withHeader("Authorization", equalTo("Bearer user-no-permission"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withBody("{\"error\":\"Accès interdit\",\"message\":\"Vous n'avez pas les permissions nécessaires pour créer une variante.\"}")));

        // PRIORITÉ 3 : Création réussie (201 CREATED)
        // C'est le cas par défaut pour les requêtes POST valides. Il doit avoir une priorité plus basse.
        server.stubFor(post(urlEqualTo("/variants"))
                .atPriority(3)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("variants/responses/post_variant_success.json")));
    }

    private static void configurePutStubs(WireMockServer server) {
        // PRIORITÉ 1 : Mise à jour sans les autorisations nécessaires (403 FORBIDDEN)
        // Intercepte toute requête PUT sur /variants/* avec le mauvais token.
        server.stubFor(put(urlPathMatching("/variants/[a-zA-Z0-9-]+"))
                .atPriority(1)
                .withHeader("Authorization", equalTo("Bearer user-no-permission"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withBody("{\"error\":\"Accès interdit\",\"message\":\"Vous n'avez pas les permissions nécessaires.\"}")));

        // PRIORITÉ 2 : Activation d'une variante (200 OK)
        // URL exacte, prioritaire sur l'URL de mise à jour générique.
        server.stubFor(put(urlPathEqualTo("/variants/variant-to-activate/activate"))
                .atPriority(2)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"variant-to-activate\",\"status\":\"ACTIVE\",\"message\":\"Variante activée avec succès.\"}")));

        // PRIORITÉ 3 : Définir un prix unitaire (200 OK)
        server.stubFor(put(urlPathEqualTo("/variants/variant-123/price"))
                .atPriority(3)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"variant-123\",\"price\":50.00}")));

        // PRIORITÉ 4 : Mise à jour réussie (200 OK)
        // Cas par défaut pour une mise à jour valide.
        server.stubFor(put(urlPathEqualTo("/variants/variant-123"))
                .atPriority(4)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("variants/responses/put_variant_success.json")));
    }

    private static void configureDeleteStubs(WireMockServer server) {
        // PRIORITÉ 1 : Suppression sans les autorisations nécessaires (403 FORBIDDEN)
        server.stubFor(delete(urlPathMatching("/variants/.*"))
                .atPriority(0)
                .withHeader("Authorization", equalTo("Bearer user-no-permission"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withBody("{\"error\":\"Accès interdit\",\"message\":\"Vous n'avez pas les permissions nécessaires.\"}")));


        // PRIORITÉ 2 : Suppression réussie (204 NO CONTENT)
        // Cas par défaut pour une suppression valide.
        server.stubFor(delete(urlEqualTo("/variants/1"))
                .atPriority(6)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .willReturn(aResponse()
                        .withStatus(204)));
    }

    private static void configureGetStubs(WireMockServer server) {
        // --- Stubs GET ---
        // 1. Lire les détails d'une variante spécifique
        server.stubFor(get(urlEqualTo("/variants/variant-123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"variant-123\",\"name\":\"Variante Chaussures Rouges\",\"status\":\"ACTIVE\"}")));

        // 2. Tenter de lire une variante inexistante
        server.stubFor(get(urlEqualTo("/variants/nonExistentVariantId"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Variante non trouvée\",\"message\":\"La variante avec l'ID spécifié n'existe pas.\"}")));

        // 3. Rechercher par nom
        server.stubFor(get(urlPathEqualTo("/variants"))
                .withQueryParam("name", equalTo("Variante Rouge"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"variant-123\",\"name\":\"Variante Chaussures Rouges\",\"status\":\"ACTIVE\"}]")));

        // 4. Filtrer par produit
        server.stubFor(get(urlPathEqualTo("/variants"))
                .withQueryParam("product", equalTo("prod-456"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"variant-123\",\"product_id\":\"prod-456\",\"name\":\"Variante Chaussures Rouges\"},{\"id\":\"variant-789\",\"product_id\":\"prod-456\",\"name\":\"Variante Chaussures Noires\"}]")));

        // 5. Lire toutes les variantes (le plus générique)
        server.stubFor(get(urlEqualTo("/variants"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"variant-123\",\"name\":\"Variante Chaussures Rouges\"},{\"id\":\"variant-789\",\"name\":\"Variante Chaussures Noires\"},{\"id\":\"variant-987\",\"name\":\"Variante Sac à main en cuir\"}]")));

    }
}