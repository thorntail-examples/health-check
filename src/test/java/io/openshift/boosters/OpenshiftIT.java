package io.openshift.boosters;

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
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
    public void test_A_ServiceInvocation() {
        when()
                .get("/greeting")
                .then()
                .assertThat().statusCode(200)
                .assertThat().body(containsString("Hello, World!"));
    }

    @Test
    public void test_B_ServiceKilled() throws Exception {
        // suspend service
        when()
                .get("/killme")
                .then()
                .assertThat().statusCode(200);

        Thread.sleep(500);

        // verify it has been suspended
        when()
                .get("/greeting")
                .then()
                .assertThat().statusCode(503);
    }
}

