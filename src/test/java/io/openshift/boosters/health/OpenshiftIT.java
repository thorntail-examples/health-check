package io.openshift.boosters.health;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

/**
 * @author Heiko Braun
 */
@RunWith(Arquillian.class)
@RunAsClient
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OpenshiftIT {

    private static final String APPLICATION_NAME = System.getProperty("app.name");

    private static final OpenshiftTestAssistant openshift = new OpenshiftTestAssistant(APPLICATION_NAME);

    @BeforeClass
    public static void setup() throws Exception {

        Assert.assertNotNull(APPLICATION_NAME);

        // the application itself
        openshift.deployApplication();

        // wait until the pods & routes become available
        openshift.awaitApplicationReadinessOrFail();

    }

    @AfterClass
    public static void teardown() throws Exception {
       openshift.cleanup();
    }

    @Test
    public void test_A_ServiceInvocation() {

        expect().
                statusCode(200).
                body(containsString("Hello, World!")).
        when().
            get(openshift.getBaseUrl() + "/api/greeting");
    }

    @Test
    public void test_B_ServiceKilled() throws Exception {

        // suspend service
        expect().
                statusCode(200).
        when().
                get(openshift.getBaseUrl() + "/api/killme");

        Thread.sleep(500);

        // verify it has been suspended
        expect().
                statusCode(503).
        when().
                get(openshift.getBaseUrl() + "/api/greeting");
    }
}

