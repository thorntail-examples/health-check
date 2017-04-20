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

package io.openshift.boosters;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import io.openshift.booster.test.OpenShiftTestAssistant;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

/**
 * @author Heiko Braun
 */
public class OpenshiftIT {

    private static final OpenShiftTestAssistant openshift = new OpenShiftTestAssistant();

    @BeforeClass
    public static void setup() throws Exception {
        // the application itself
        openshift.deployApplication();

        // wait until the pods & routes become available
        openshift.awaitApplicationReadinessOrFail();

        await().atMost(5, TimeUnit.MINUTES).until(() -> {
            try {
                Response response = get();
                return response.getStatusCode() == 200;
            } catch (Exception e) {
                return false;
            }
        });

        RestAssured.baseURI = RestAssured.baseURI + "/api";
    }

    @AfterClass
    public static void teardown() throws Exception {
        openshift.cleanup();
    }

    @Test
    public void testServiceInvocation() {
        when()
                .get("/greeting")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body(containsString("Hello, World!"));
    }

    @Test
    public void testServiceKilledAndRestarted() throws Exception {
        when()
                .get("/greeting")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body(containsString("Hello, World!"));

        // suspend service
        when()
                .get("/killme")
                .then()
                .assertThat().statusCode(200);

        awaitStatus(503, Duration.ofSeconds(30));

        long begin = System.currentTimeMillis();
        awaitStatus(200, Duration.ofMinutes(3));
        long end = System.currentTimeMillis();
        System.out.println("Failure recovered in " + (end - begin) + " ms");
    }

    private void awaitStatus(int status, Duration duration) {
        await().atMost(duration.getSeconds(), TimeUnit.SECONDS).until(() -> {
            try {
                Response response = get("/greeting");
                return response.getStatusCode() == status;
            } catch (Exception e) {
                return false;
            }
        });
    }
}
