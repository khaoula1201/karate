package com.exemple.qa.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URL;
import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class VariantStubs {

    public static void configure(WireMockServer server) {
        configurePostStubs(server);
        configurePutStubs(server);
        configureDeleteStubs(server);
        configureGetStubs(server);
    }

    private static void configurePostStubs(WireMockServer server) {

        // PRIORITÉ 1 : Données invalides - characteristics_invalid == true
        server.stubFor(post(urlEqualTo("/variants"))
                .atPriority(1)
                .withHeader("Content-Type", containing("application/json"))
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .withRequestBody(matchingJsonPath("$.characteristics_invalid", equalTo("true")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Données de variante invalides\",\"message\":\"Les caractéristiques ou l'emballage spécifiés sont invalides.\"}")));

        // PRIORITÉ 2 : Données invalides - packaging/category/brand invalides (ton stub existant, priorité abaissée)
        server.stubFor(post(urlEqualTo("/variants"))
                .atPriority(2)
                .withHeader("Content-Type", containing("application/json"))
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .withRequestBody(matchingJsonPath("$.packaging_id", equalTo("INVALID_PACK_ID")))
                // NOTE: chaque withRequestBody est un ET, donc je double les stubs pour category / brand séparément ci-dessous
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Données de variante invalides\",\"message\":\"Les caractéristiques ou l'emballage spécifiés sont invalides.\"}")));

        // PRIORITÉ 2b : Données invalides - category invalide (si tu veux capter ce cas sans packaging)
        server.stubFor(post(urlEqualTo("/variants"))
                .atPriority(2)
                .withHeader("Content-Type", containing("application/json"))
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .withRequestBody(matchingJsonPath("$.category_id", equalTo("INVALID_CAT_ID")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Données de variante invalides\",\"message\":\"Les caractéristiques ou l'emballage spécifiés sont invalides.\"}")));

        // PRIORITÉ 2c : Données invalides - brand invalide
        server.stubFor(post(urlEqualTo("/variants"))
                .atPriority(2)
                .withHeader("Content-Type", containing("application/json"))
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .withRequestBody(matchingJsonPath("$.brand_id", equalTo("INVALID_BRAND_ID")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Données de variante invalides\",\"message\":\"Les caractéristiques ou l'emballage spécifiés sont invalides.\"}")));

        // PRIORITÉ 5 : Auth manquante / mal formée (ton code disait 403 mais status=401 -> je garde 401 comme dans BodyFile)
        server.stubFor(post(urlEqualTo("/variants"))
                .atPriority(5)
                .withHeader("Authorization", notMatching("Bearer .+"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("common/error_unauthorized.json")));

        // PRIORITÉ 6 : Succès (défaut)
        server.stubFor(post(urlEqualTo("/variants"))
                .atPriority(6)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .withHeader("Content-Type", containing("application/json"))
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

    public static void configureGetStubs(WireMockServer server) {

        // PRIORITÉ 1: Les requêtes les plus spécifiques (avec des paramètres de recherche)
        server.stubFor(get(urlPathEqualTo("/variants"))
                .atPriority(1)
                .withQueryParam("name", equalTo("Variante Rouge"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"variant-123\",\"name\":\"Variante Chaussures Rouges\",\"status\":\"ACTIVE\"}]")));

        server.stubFor(get(urlPathEqualTo("/variants"))
                .atPriority(1)
                .withQueryParam("product", equalTo("prod-456"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"variant-123\",\"product_id\":\"prod-456\",\"name\":\"Variante Chaussures Rouges\"},{\"id\":\"variant-789\",\"product_id\":\"prod-456\",\"name\":\"Variante Chaussures Noires\"}]")));

        // PRIORITÉ 2: Récupérer une variante par ID (cas de succès)
        server.stubFor(get(urlEqualTo("/variants/variant-123"))
                .atPriority(2)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"variant-123\",\"name\":\"Variante Chaussures Rouges\",\"status\":\"ACTIVE\"}")));

        // PRIORITÉ 3: Liste complète de toutes les variantes.
        // C'est le stub qui correspond à `GET /variants` sans AUCUN paramètre.
        server.stubFor(get(urlPathEqualTo("/variants"))
                .atPriority(3)
                .withQueryParams(new HashMap<>())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"variant-123\",\"name\":\"Variante Chaussures Rouges\"},{\"id\":\"variant-789\",\"name\":\"Variante Chaussures Noires\"},{\"id\":\"variant-987\",\"name\":\"Variante Sac à main en cuir\"}]")));
        // PRIORITÉ 4: Le "catch-all" pour les IDs non trouvés ou les requêtes malformées
        server.stubFor(get(urlPathMatching("/variants/.*"))
                .atPriority(4)
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Variante non trouvée\",\"message\":\"La variante avec l'ID spécifié n'existe pas.\"}")));
    }
}