package com.exemple.qa.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public class MockServer {
    public static void main(String[] args) {
        WireMockServer wireMock = new WireMockServer(8080);
        FileSource filesFileSource = new SingleRootFileSource("src/test/resources");
        wireMock.enableRecordMappings(filesFileSource, filesFileSource);
        wireMock.start();
        configurePostEndpoints(wireMock);
        System.out.println("WireMock démarré : http://localhost:8080");
    }
    private static void configurePostEndpoints(WireMockServer wireMock) {
        wireMock.stubFor(post(urlEqualTo("/products"))
                .atPriority(100)
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withBodyFile("post/POST_product_response_success.json")));

    }
}
