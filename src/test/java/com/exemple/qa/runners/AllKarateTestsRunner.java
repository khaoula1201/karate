package com.exemple.qa.runners;

import com.exemple.qa.wiremock.MockServer;
import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class AllKarateTestsRunner {

    @BeforeAll
    public static void setUp() {
        MockServer.start();
        System.setProperty("karate.wiremock.port", String.valueOf(MockServer.MOCK_PORT));
    }

    @AfterAll
    public static void tearDown() {
        MockServer.stop();
    }

    @Karate.Test
    Karate testAllFeatures() {
        return Karate.run("classpath:features/images");

    }
}