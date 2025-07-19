package com.exemple.qa.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class StockStubs {
    public static void configure(WireMockServer server) {
        configureCreateStockStubs(server);
       configureDeleteStockStubs(server);
      configureUpdateStockStubs(server);
        configureReadStockStubs(server);
    }
    public static void configureDeleteStockStubs(WireMockServer server) {

        // --- STUB 1 : Erreur de conflit (haute priorité) ---
        // Ce stub est le plus spécifique des échecs et doit être traité en premier.
        server.stubFor(delete(urlEqualTo("/stocks/stock-in-use-456"))
                .atPriority(1)
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Conflict\",\"message\":\"Le stock est actuellement utilise et ne peut pas etre supprime.\"}")));

        // --- STUB 2 : Succès (priorité moyenne) ---
        // Simule une suppression réussie.
        server.stubFor(delete(urlEqualTo("/stocks/stock-to-delete-123"))
                .atPriority(2)
                .willReturn(aResponse()
                        .withStatus(204)
                        .withBody(""))); // Une réponse 204 n'a pas de corps

        // --- STUB 3 : Not Found (faible priorité) ---
        // C'est un stub générique pour toutes les requêtes DELETE.
        // Il est en dernière priorité pour ne pas écraser les stubs spécifiques ci-dessus.
        server.stubFor(delete(urlMatching("/stocks/.*"))
                .atPriority(3)
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Not Found\",\"message\":\"Le stock avec l'ID specifie n'existe pas.\"}")));
    }
    public static void configureUpdateStockStubs(WireMockServer server) {

        // --- STUB 1 : Erreur de validation (haute priorité) ---
        // Simule un échec de validation pour une requête PUT sur l'ID 123.
        server.stubFor(put(urlEqualTo("/stocks/stock-123"))
                .atPriority(1)
                .withRequestBody(matchingJsonPath("$.quantity", matching("-.*")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"La quantite doit etre un nombre positif.\"}")));

        // --- STUB 2 : Succès (priorité moyenne) ---
        // Simule une mise à jour réussie pour l'ID 123 si la requête est valide.
        server.stubFor(put(urlEqualTo("/stocks/stock-123"))
                .atPriority(2)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"stock-123\",\"quantity\":75,\"location\":\"WH-A\"}")));

        // --- STUB 3 : Not Found (faible priorité) ---
        // C'est un stub générique pour toutes les requêtes PUT sur les stocks.
        // Il doit être en dernière priorité pour ne pas écraser les stubs spécifiques ci-dessus.
        server.stubFor(put(urlMatching("/stocks/.*"))
                .atPriority(3)
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Not Found\",\"message\":\"Le stock avec l'ID spécifie n'existe pas.\"}")));
    }
    public static void configureCreateStockStubs(WireMockServer server) {

        // --- STUB 1 : Erreurs de sécurité (haute priorité) ---
        // Le serveur vérifie l'authentification en premier.

        // 401 Unauthorized - Jeton manquant
        server.stubFor(post(urlEqualTo("/stocks"))
                .atPriority(1)
                .withHeader("Authorization", notMatching("Bearer .+"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Unauthorized\",\"message\":\"Un jeton d'authentification est requis.\"}")));

        // 401 Unauthorized - Jeton invalide
        server.stubFor(post(urlEqualTo("/stocks"))
                .atPriority(1)
                .withHeader("Authorization", equalTo("Bearer invalid-token"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Unauthorized\",\"message\":\"Le jeton d'authentification est invalide.\"}")));

        // 403 Forbidden - Permissions insuffisantes
        server.stubFor(post(urlEqualTo("/stocks"))
                .atPriority(1)
                .withHeader("Authorization", equalTo("Bearer user-no-permission"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Forbidden\",\"message\":\"Vous n'avez pas les permissions nécessaires pour créer un stock.\"}")));

        // --- STUB 2 : Erreurs de validation (priorité moyenne) ---
        // Ces stubs sont déclenchés avant le cas de succès pour gérer les requêtes invalides.

        // 400 Bad Request - Corps de requête vide ou manquant
        server.stubFor(post(urlEqualTo("/stocks"))
                .atPriority(1)
                .withRequestBody(equalToJson("{}"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"Le corps de la requête ne peut pas être vide.\"}")));

        // 400 Bad Request - Champ obligatoire manquant (exemple: product_id)
        server.stubFor(post(urlEqualTo("/stocks"))
                .atPriority(2)
                .withRequestBody(matchingJsonPath("$.product_id", absent())) // Matcher avancé
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"Le champ 'product_id' est obligatoire.\"}")));

        // 400 Bad Request - Type de données incorrect pour 'quantity'
        server.stubFor(post(urlEqualTo("/stocks"))
                .atPriority(2)
                .withRequestBody(matchingJsonPath("$.quantity", not(matching("\\d+"))))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"Le champ 'quantity' doit être un nombre entier.\"}")));

        // 400 Bad Request - Quantité négative
        server.stubFor(post(urlEqualTo("/stocks"))
                .atPriority(2)
                .withRequestBody(matchingJsonPath("$.quantity", matching("-.*")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"La quantité doit être positive.\"}")));

        // --- STUB 3 : Conflit (priorité moyenne) ---
        // Gère le cas où un stock pour le même produit/localisation existe déjà.
        server.stubFor(post(urlEqualTo("/stocks"))
                .atPriority(3)
                .withRequestBody(equalToJson("{\"product_id\":\"prod-exist\",\"quantity\":20,\"location\":\"WH-A\"}"))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Conflict\",\"message\":\"Un stock existe déjà pour ce produit et cette localisation.\"}")));

        // --- STUB 4 : Succès (faible priorité) ---
        // Le cas par défaut, qui est déclenché si aucune des conditions ci-dessus n'est remplie.
        server.stubFor(post(urlEqualTo("/stocks"))
                .atPriority(4)
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"stock-generated-id-123\",\"product_id\":\"prod-456\",\"quantity\":50,\"location\":\"WH-A\",\"status\":\"AVAILABLE\"}")));
    }
    public static void configureReadStockStubs(WireMockServer server) {

        // --- STUB 1 : Lire le stock d'un produit spécifique (par ID) ---
        server.stubFor(get(urlEqualTo("/stocks/stock-123"))
                .atPriority(1)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"stock-123\",\"product_id\":\"prod-456\",\"quantity\":50,\"location\":\"WH-A\"}")));

        // --- STUB 2 : Lire le stock d'un produit inexistant ---
        server.stubFor(get(urlMatching("/stocks/.*"))
                .atPriority(2) // Priorité inférieure pour attraper tout ce qui n'a pas été trouvé par le stub 1
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Not Found\",\"message\":\"Le stock avec l'ID spécifié n'existe pas.\"}")));


        // --- STUB 3 : Rechercher un stock par ID de produit ---
        server.stubFor(get(urlPathEqualTo("/stocks"))
                .atPriority(3)
                .withQueryParam("product_id", equalTo("prod-101"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"stock-101\",\"product_id\":\"prod-101\",\"quantity\":15,\"location\":\"WH-C\"}]")));


        // --- STUB 4 : Filtrer les stocks par localisation ---
        server.stubFor(get(urlPathEqualTo("/stocks"))
                .atPriority(3)
                .withQueryParam("location", equalTo("WH-B"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"stock-456\",\"product_id\":\"prod-789\",\"quantity\":100,\"location\":\"WH-B\"},{\"id\":\"stock-789\",\"product_id\":\"prod-123\",\"quantity\":5,\"location\":\"WH-B\"}]")));

        // --- STUB 5 : Filtrer les stocks par quantité (supérieure à 20) ---
        server.stubFor(get(urlPathEqualTo("/stocks"))
                .atPriority(3)
                .withQueryParam("quantity_gt", equalTo("20"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"stock-123\",\"product_id\":\"prod-456\",\"quantity\":50,\"location\":\"WH-A\"},{\"id\":\"stock-456\",\"product_id\":\"prod-789\",\"quantity\":100,\"location\":\"WH-B\"}]")));

        // --- STUB 6 : Combiner les filtres ---
        server.stubFor(get(urlPathEqualTo("/stocks"))
                .atPriority(3)
                .withQueryParam("product_id", equalTo("prod-456"))
                .withQueryParam("location", equalTo("WH-A"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"stock-123\",\"product_id\":\"prod-456\",\"quantity\":50,\"location\":\"WH-A\"}]")));


        // --- STUB 7 : Liste complète de tous les stocks ---
        // Ce stub est le plus générique, il doit être en dernière priorité pour ne pas interférer
        server.stubFor(get(urlEqualTo("/stocks"))
                .atPriority(4)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"stock-123\",\"product_id\":\"prod-456\",\"quantity\":50,\"location\":\"WH-A\"},{\"id\":\"stock-456\",\"product_id\":\"prod-789\",\"quantity\":100,\"location\":\"WH-B\"},{\"id\":\"stock-789\",\"product_id\":\"prod-123\",\"quantity\":5,\"location\":\"WH-B\"}]")));
    }
}

