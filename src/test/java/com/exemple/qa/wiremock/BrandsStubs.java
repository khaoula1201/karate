package com.exemple.qa.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class BrandsStubs {

    public static void configureAllStubs(WireMockServer server) {
        configureReadBrandsStubs(server);
        configureCreateBrandsStubs(server);
        configureUpdateBrandsStubs(server);
        configureDeleteBrandsStubs(server);
    }

    private static void configureReadBrandsStubs(WireMockServer server) {
        // --- GET /brands/{id} - Cas d'une marque spécifique existante ---
        server.stubFor(get(urlEqualTo("/brands/brand-1"))
                .atPriority(1)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"brand-1\",\"name\":\"Brand A\"}")));

        // --- GET /brands - Cas de la liste complète des marques ---
        server.stubFor(get(urlEqualTo("/brands"))
                .atPriority(2)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"brand-1\",\"name\":\"Brand A\"},{\"id\":\"brand-2\",\"name\":\"Brand B\"}]")));

        // --- GET /brands/{id} - Cas générique (Not Found) ---
        server.stubFor(get(urlMatching("/brands/.*"))
                .atPriority(3)
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Not Found\",\"message\":\"La marque n'existe pas.\"}")));
    }

    private static void configureCreateBrandsStubs(WireMockServer server) {
        // --- POST /brands - Erreur de validation (priorité haute) ---
        server.stubFor(post(urlEqualTo("/brands"))
                .atPriority(0)
                .withRequestBody(equalToJson("{}"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"Le corps de la requête ne peut pas être vide.\"}")));

        server.stubFor(post(urlEqualTo("/brands"))
                .atPriority(1)
                .withRequestBody(matchingJsonPath("$.name", absent()))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"Le nom de la marque ne peut pas être vide.\"}")));

        server.stubFor(post(urlEqualTo("/brands"))
                .atPriority(1)
                .withRequestBody(matchingJsonPath("$.name", equalTo("")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"Le nom de la marque ne peut pas être vide.\"}")));

        // --- POST /brands - Conflit ---
        server.stubFor(post(urlEqualTo("/brands"))
                .atPriority(2)
                .withRequestBody(matchingJsonPath("$.name", equalTo("Brand A")))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Conflict\",\"message\":\"Une marque avec ce nom existe deja.\"}")));

        // --- POST /brands - Succès (par défaut) ---
        server.stubFor(post(urlEqualTo("/brands"))
                .atPriority(3)
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"brand-3\",\"name\":\"New Brand\"}")));
    }

    private static void configureUpdateBrandsStubs(WireMockServer server) {
        // --- PUT /brands/{id} - Erreur de validation (priorité haute) ---
        server.stubFor(put(urlEqualTo("/brands/brand-1"))
                .atPriority(1)
                .withRequestBody(matchingJsonPath("$.name", equalTo("")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"Le nom de la marque ne peut pas etre vide.\"}")));

        // --- PUT /brands/{id} - Succès ---
        server.stubFor(put(urlEqualTo("/brands/brand-1"))
                .atPriority(2)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"brand-1\",\"name\":\"Brand A - Updated\"}")));

        // --- PUT /brands/{id} - Cas générique (Not Found) ---
        server.stubFor(put(urlMatching("/brands/.*"))
                .atPriority(3)
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Not Found\",\"message\":\"La marque n'existe pas.\"}")));
    }

    private static void configureDeleteBrandsStubs(WireMockServer server) {
        // --- DELETE /brands/{id} - Conflit (priorité haute) ---
        server.stubFor(delete(urlEqualTo("/brands/brand-in-use"))
                .atPriority(1)
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Conflict\",\"message\":\"La marque est utilisee et ne peut pas etre supprimee.\"}")));

        // --- DELETE /brands/{id} - Succès ---
        server.stubFor(delete(urlEqualTo("/brands/brand-to-delete"))
                .atPriority(2)
                .willReturn(aResponse()
                        .withStatus(204)));

        // --- DELETE /brands/{id} - Not Found ---
        server.stubFor(delete(urlMatching("/brands/.*"))
                .atPriority(3)
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Not Found\",\"message\":\"La marque n'existe pas.\"}")));
    }
}