/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.thorntail.example;


import io.restassured.RestAssured;
import io.thorntail.openshift.test.OpenShiftTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.when;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;

@OpenShiftTest
public class OpenshiftIT {
    @BeforeAll
    public static void setUp() {
        RestAssured.basePath = "/api";
    }

    @BeforeEach
    public void awaitEndpoint() {
        awaitStatus(200, Duration.ofMinutes(1));
    }

    @Test
    public void serviceInvocation() {
        when()
                .get("/greeting")
        .then()
                .statusCode(200)
                .body(containsString("Hello, World!"));
    }

    @Test
    public void serviceStoppedAndRestarted() {
        when()
                .get("/greeting")
        .then()
                .statusCode(200)
                .body(containsString("Hello, World!"));

        // suspend service
        when()
                .get("/stop")
        .then()
                .statusCode(200);

        awaitStatus(503, Duration.ofSeconds(30));

        long begin = System.currentTimeMillis();
        awaitStatus(200, Duration.ofMinutes(3));
        long end = System.currentTimeMillis();
        System.out.println("Failure recovered in " + (end - begin) + " ms");
    }

    private void awaitStatus(int expectedStatus, Duration duration) {
        await().atMost(duration.getSeconds(), TimeUnit.SECONDS).until(() -> {
            try {
                int statusCode =
                        when()
                                .get("/greeting")
                        .then()
                                .extract().statusCode();
                return statusCode == expectedStatus;
            } catch (Exception e) {
                return false;
            }
        });
    }
}
