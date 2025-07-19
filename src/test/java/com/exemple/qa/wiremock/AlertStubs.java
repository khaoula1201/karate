package com.exemple.qa.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.anything;

public class AlertStubs {
    public static void configureAlerts(WireMockServer server) {
        configurePostAlerts(server);
      configureUpdateAlerts(server);
     configureDeleteAlerts(server);
      configureReadAlerts(server);
    }

    public static void configurePostAlerts(WireMockServer server) {

        // 1. 403 - permissions insuffisantes
        server.stubFor(post(urlEqualTo("/alerts"))
                .atPriority(1)
                .withHeader("Authorization", equalTo("Bearer user-no-permission"))
                .willReturn(jsonResponse(403,
                        "{\"error\":\"Forbidden\",\"message\":\"Vous n'avez pas les permissions nécessaires pour créer une alerte.\"}")));

        // 2. 401 - token invalide
        server.stubFor(post(urlEqualTo("/alerts"))
                .atPriority(2)
                .withHeader("Authorization", equalTo("Bearer invalid-token"))
                .willReturn(jsonResponse(401,
                        "{\"error\":\"Unauthorized\",\"message\":\"Le jeton d'authentification est invalide ou a expiré.\"}")));

        // 3. 401 - token absent
        server.stubFor(post(urlEqualTo("/alerts"))
                .atPriority(3)
                .withHeader("Authorization", notMatching("Bearer .+"))
                .willReturn(jsonResponse(401,
                        "{\"error\":\"Unauthorized\",\"message\":\"Un jeton d'authentification est requis.\"}")));

        // 4. 400 - corps vide {}
        server.stubFor(post(urlEqualTo("/alerts"))
                .atPriority(4)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .withRequestBody(equalTo("{}"))
                .willReturn(jsonResponse(400,
                        "{\"error\":\"Invalid Request Body\",\"message\":\"Le corps de la requête ne peut pas être vide ou est mal formé.\"}")));

        // 5. 400 - type d'alerte invalide
        server.stubFor(post(urlEqualTo("/alerts"))
                .atPriority(5)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .withRequestBody(matchingJsonPath("$.type", equalTo("INVALID_TYPE")))
                .willReturn(jsonResponse(400,
                        "{\"error\":\"Invalid Alert Type\",\"message\":\"Le type d'alerte spécifié est invalide.\"}")));

        // 6. 400 - champs requis manquants
        server.stubFor(post(urlEqualTo("/alerts"))
                .atPriority(6)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .withRequestBody(notMatching("(?s).*\"type\".*\"severity\".*\"product_id\".*\"message\".*"))
                .willReturn(jsonResponse(400,
                        "{\"error\":\"Missing Required Fields\",\"message\":\"Les champs obligatoires (type, severity, product_id, message) sont requis.\"}")));

        // 7. 201 - succès complet (tous champs)
        server.stubFor(post(urlEqualTo("/alerts"))
                .atPriority(7)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .withHeader("Content-Type", matching("application/json.*"))
                .withRequestBody(matchingJsonPath("$.type"))
                .withRequestBody(matchingJsonPath("$.severity"))
                .withRequestBody(matchingJsonPath("$.product_id"))
                .withRequestBody(matchingJsonPath("$.message"))
                .willReturn(jsonResponse(201,
                        "{\"id\":\"alert-12345\",\"type\":\"STOCK_LOW\",\"severity\":\"HIGH\",\"product_id\":\"prod-456\",\"status\":\"ACTIVE\",\"message\":\"Stock faible pour le produit prod-456.\"}")));

        // 8. 201 - succès avec champs optionnels (si tu veux distinguer)
        server.stubFor(post(urlEqualTo("/alerts"))
                .atPriority(8)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .withRequestBody(matchingJsonPath("$.optionalField", matching(".*")))
                .willReturn(jsonResponse(201,
                        "{\"id\":\"alert-12346\",\"type\":\"STOCK_LOW\",\"severity\":\"HIGH\",\"product_id\":\"prod-456\",\"status\":\"ACTIVE\",\"message\":\"Stock faible pour le produit prod-456.\", \"optionalField\": \"someValue\"}")));
    }

    private static ResponseDefinitionBuilder jsonResponse(int status, String body) {
        return aResponse()
                .withStatus(status)
                .withHeader("Content-Type", "application/json")
                .withBody(body);
    }



    private static void configureReadAlerts(WireMockServer server) {

        // Cas 1 : Récupération d'une alerte spécifique
        server.stubFor(get(urlEqualTo("/alerts/alert-12345"))
                .atPriority(1)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"alert-12345\",\"type\":\"STOCK_LOW\",\"severity\":\"HIGH\",\"product_id\":\"prod-456\",\"status\":\"ACTIVE\",\"message\":\"Alerte de stock faible pour le produit prod-456.\"}")));

        // Cas 2 : Alerte non trouvée
        server.stubFor(get(urlMatching("/alerts/.*"))
                .atPriority(2)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Not Found\",\"message\":\"L'alerte spécifiée n'existe pas.\"}")));
    }


    private static void configureDeleteAlerts(WireMockServer server) {

            // Cas 1 : Suppression réussie d'une alerte
            server.stubFor(delete(urlEqualTo("/alerts/alert-12345"))
                    .atPriority(1)
                    .withHeader("Authorization", equalTo("Bearer admin-token"))
                    .willReturn(aResponse()
                            .withStatus(204)
                            .withHeader("Content-Type", "application/json")));

            // Cas 2 : Alerte non trouvée
            server.stubFor(delete(urlMatching("/alerts/non-existent-alert"))
                    .atPriority(2)
                    .withHeader("Authorization", equalTo("Bearer admin-token"))
                    .willReturn(aResponse()
                            .withStatus(404)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"error\":\"Not Found\",\"message\":\"L'alerte spécifiée n'existe pas.\"}")));

            // Cas 3 : Permissions insuffisantes
            server.stubFor(delete(urlMatching("/alerts/alert-12345"))
                    .atPriority(3)
                    .withHeader("Authorization", equalTo("Bearer user-no-permission"))
                    .willReturn(aResponse()
                            .withStatus(403)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"error\":\"Forbidden\",\"message\":\"Vous n'avez pas les permissions nécessaires pour supprimer cette alerte.\"}")));
        }


    public static void configureUpdateAlerts(WireMockServer server) {

        // Cas 1 : Mise à jour réussie d'une alerte
        server.stubFor(put(urlEqualTo("/alerts/alert-12345"))
                .atPriority(1)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .withRequestBody(matchingJsonPath("$.status", equalTo("INACTIVE")))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"alert-12345\",\"type\":\"STOCK_LOW\",\"severity\":\"HIGH\",\"product_id\":\"prod-456\",\"status\":\"INACTIVE\",\"message\":\"Alerte mise à jour avec le statut INACTIVE.\"}")));

        // Cas 2 : Alerte non trouvée
        server.stubFor(put(urlMatching("/alerts/non-existent-alert"))
                .atPriority(2)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Not Found\",\"message\":\"L'alerte spécifiée n'existe pas.\"}")));

        // Cas 3 : Données de requête invalides (par exemple, corps vide)
        server.stubFor(put(urlEqualTo("/alerts/alert-12345"))
                .atPriority(3)
                .withHeader("Authorization", equalTo("Bearer admin-token"))
                .withRequestBody(equalTo("{}"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"Le corps de la requête est vide ou malformé.\"}")));

        // Cas 4 : Permissions insuffisantes
        server.stubFor(put(urlEqualTo("/alerts/alert-12345"))
                .atPriority(4)
                .withHeader("Authorization", equalTo("Bearer user-no-permission"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Forbidden\",\"message\":\"Vous n'avez pas les permissions nécessaires pour modifier cette alerte.\"}")));
    }


   
}
