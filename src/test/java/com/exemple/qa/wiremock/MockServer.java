package com.exemple.qa.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;


public class MockServer {
    private static WireMockServer wireMock; // static car on aura 1 seul pr tte lapp
    public static final int MOCK_PORT = 8080;

    public static void start() {
        if (wireMock == null) {
            wireMock = new WireMockServer(MOCK_PORT);
            FileSource filesFileSource = new SingleRootFileSource("src/test/resources");
            wireMock.enableRecordMappings(filesFileSource, filesFileSource);
            wireMock.start();

            // Appel des classes de stubs dédiées.
            ProductStubs.configure(wireMock);
            VariantStubs.configure(wireMock);
            AlertStubs.configureAlerts(wireMock);
            StockStubs.configure(wireMock);
            StoreStubs.configureAllStubs(wireMock);
            BrandsStubs.configureAllStubs(wireMock);
            ImagesStubs.configureAllStubs(wireMock);


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


}