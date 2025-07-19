package com.exemple.qa.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import java.util.Base64;

public class ImagesStubs {

    public static void configureAllStubs(WireMockServer server) {
        configureUploadImageStubs(server);
        configureReadImageStubs(server);
        configureDeleteImageStubs(server);
    }

    private static void configureUploadImageStubs(WireMockServer server) {
        // --- POST /images - Cas d'erreur (requête mal formatee, haute priorite) ---
        server.stubFor(post(urlEqualTo("/images"))
                .atPriority(0)
                .withHeader("Content-Type", notMatching("multipart/form-data.*"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"Le corps de la requete doit etre multipart/form-data et contenir un fichier.\"}")));

        // --- POST /images - Cas d'erreur (type de fichier invalide) ---
        // On simule l'erreur en cherchant une extension .txt dans la requête
        server.stubFor(post(urlEqualTo("/images"))
                .atPriority(1)
                .withRequestBody(containing(".txt"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\",\"message\":\"Le type de fichier n'est pas une image valide.\"}")));

        // --- POST /images - Cas de succès ---
        server.stubFor(post(urlEqualTo("/images"))
                .atPriority(2)
                .withHeader("Content-Type", matching("multipart/form-data.*"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"image-uuid-123\",\"url\":\"http://localhost:8080/images/image-uuid-123.jpg\"}")));
    }

    private static void configureReadImageStubs(WireMockServer server) {
        // Un faux contenu d'image en Base64 pour l'exemple
        String base64Image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";

        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        // --- GET /images/{id} - Cas de succès ---
        server.stubFor(get(urlEqualTo("/images/image-123"))
                .atPriority(1)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "image/jpeg")
                        .withBody(imageBytes)));

        // --- GET /images/{id} - Not Found ---
        server.stubFor(get(urlMatching("/images/.*"))
                .atPriority(2)
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Not Found\",\"message\":\"L'image n'a pas ete trouvee.\"}")));
    }

    private static void configureDeleteImageStubs(WireMockServer server) {
        // --- DELETE /images/{id} - Succès ---
        server.stubFor(delete(urlEqualTo("/images/image-to-delete-456"))
                .atPriority(1)
                .willReturn(aResponse()
                        .withStatus(204)));

        // --- DELETE /images/{id} - Not Found ---
        server.stubFor(delete(urlMatching("/images/.*"))
                .atPriority(2)
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Not Found\",\"message\":\"L'image n'a pas ete trouvee.\"}")));
    }
}