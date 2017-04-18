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
