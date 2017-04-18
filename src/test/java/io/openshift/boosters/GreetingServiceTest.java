package io.openshift.boosters;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;

/**
 * @author Heiko Braun
 */
@RunWith(Arquillian.class)
@DefaultDeployment
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GreetingServiceTest {

    @Test
    @RunAsClient
    public void test_A_service_invocation() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("api").path("greeting");

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.readEntity(String.class).contains("Hello, World!"));
    }

    @Test
    @RunAsClient
    public void test_B_service_killed() throws Exception {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget killme = client.target("http://localhost:8080")
                    .path("api").path("killme");

            // suspend process
            Response response = killme.request().get();
            Assert.assertEquals(200, response.getStatus());
        } finally {
            client.close();
        }

        awaitStatus(503, Duration.ofSeconds(10));
    }

    private void awaitStatus(int status, Duration duration) {
        await().atMost(duration.getSeconds(), TimeUnit.SECONDS).until(() -> {
            Client client = ClientBuilder.newClient(); // new connection
            try {
                WebTarget greeting = client.target("http://localhost:8080")
                        .path("api").path("greeting");

                Response response = greeting.request().get();
                return response.getStatus() == status;
            } finally {
                client.close();
            }
        });
    }
}
