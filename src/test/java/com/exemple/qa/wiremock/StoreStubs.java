package com.exemple.qa.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class StoreStubs {

    /**
     * Méthode principale pour configurer tous les stubs de l'API Store.
     * @param server L'instance de WireMockServer.
     */
    public static void configureAllStubs(WireMockServer server) {
        configureReadStoreStubs(server);
        configureCreateStoreStubs(server);
        configureUpdateStoreStubs(server);
        configureDeleteStoreStubs(server);
    }

    private static void configureReadStoreStubs(WireMockServer server) {
        // --- GET /stores/{id} - Cas d'un magasin spécifique existant ---
        server.stubFor(get(urlEqualTo("/stores/store-1"))
                .atPriority(1)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"store-1\",\"name\":\"Store A\",\"location\":\"Paris\"}")));

        // --- GET /stores - Cas de la liste complète des magasins ---
        server.stubFor(get(urlEqualTo("/stores"))
                .atPriority(2)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"store-1\",\"name\":\"Store A\",\"location\":\"Paris\"},{\"id\":\"store-2\",\"name\":\"Store B\",\"location\":\"Lyon\"}]")));

        // --- GET /stores/{id} - Cas générique (Not Found) ---
        server.stubFor(get(urlMatching("/stores/.*"))
                .atPriority(3)
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Not Found\",\"message\":\"Le magasin n'existe pas.\"}")));
    }

    private static void configureCreateStoreStubs(WireMockServer server) {
        // --- STUB 1 : Erreur de validation (priorité la plus haute) ---
        // Ce stub doit être en priorité 0 pour être sûr de passer en premier.
        server.stubFor(post(urlEqualTo("/stores"))
                .atPriority(0) // <-- CHANGEZ CETTE LIGNE
                .withRequestBody(equalToJson("{}"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"Le corps de la requête ne peut pas être vide.\"}")));

        // 400 Bad Request - Champ obligatoire 'name' manquant
        server.stubFor(post(urlEqualTo("/stores"))
                .atPriority(1)
                .withRequestBody(matchingJsonPath("$.name", absent()))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"Le champ 'name' est obligatoire.\"}")));

        // 400 Bad Request - Champ 'name' présent mais vide
        server.stubFor(post(urlEqualTo("/stores"))
                .atPriority(1)
                .withRequestBody(matchingJsonPath("$.name", equalTo("")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"Le nom du magasin ne peut pas etre vide.\"}")));

        // --- STUB 2 : Conflit (priorité moyenne) ---
        server.stubFor(post(urlEqualTo("/stores"))
                .atPriority(2)
                .withRequestBody(matchingJsonPath("$.name", equalTo("Store A")))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Conflict\",\"message\":\"Un magasin avec ce nom existe déjà.\"}")));

        // --- STUB 3 : Succès (faible priorité, cas par défaut) ---
        server.stubFor(post(urlEqualTo("/stores"))
                .atPriority(3)
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"store-3\",\"name\":\"New Store\",\"location\":\"Marseille\"}")));
    }
    private static void configureUpdateStoreStubs(WireMockServer server) {
        // --- PUT /stores/{id} - Cas d'erreur (validation, haute priorité) ---
        server.stubFor(put(urlEqualTo("/stores/store-1"))
                .atPriority(1)
                .withRequestBody(matchingJsonPath("$.name", equalTo("")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"Le nom du magasin ne peut pas etre vide.\"}")));

        // --- PUT /stores/{id} - Cas de succès ---
        server.stubFor(put(urlEqualTo("/stores/store-1"))
                .atPriority(2)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"store-1\",\"name\":\"Store A - Updated\",\"location\":\"Paris\"}")));

        // --- PUT /stores/{id} - Cas générique (Not Found) ---
        server.stubFor(put(urlMatching("/stores/.*"))
                .atPriority(3)
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Not Found\",\"message\":\"Le magasin n'existe pas.\"}")));
    }

    private static void configureDeleteStoreStubs(WireMockServer server) {
        // --- DELETE /stores/{id} - Cas de conflit (haute priorité) ---
        server.stubFor(delete(urlEqualTo("/stores/store-in-use"))
                .atPriority(1)
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Conflict\",\"message\":\"Le magasin est en cours d'utilisation.\"}")));

        // --- DELETE /stores/{id} - Cas de succès ---
        server.stubFor(delete(urlEqualTo("/stores/store-to-delete"))
                .atPriority(2)
                .willReturn(aResponse()
                        .withStatus(204)));

        // --- DELETE /stores/{id} - Cas générique (Not Found) ---
        server.stubFor(delete(urlMatching("/stores/.*"))
                .atPriority(3)
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Not Found\",\"message\":\"Le magasin n'existe pas.\"}")));
    }
}