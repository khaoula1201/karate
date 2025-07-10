package com.exemple.qa.runners;

import com.exemple.qa.wiremock.MockServer;
import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeAll;

public class KarateRunnerTest {

    @BeforeAll
    public static void setUp() {
        MockServer.main(new String[]{});

    }
    @Karate.Test
    Karate testAllFeatures() {
        return Karate.run("classpath:features/products/createProduct.feature")

                .relativeTo(getClass());


    }

}
