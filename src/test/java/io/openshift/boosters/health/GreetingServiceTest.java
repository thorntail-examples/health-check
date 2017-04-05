package io.openshift.boosters.health;

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
        WebTarget killme = client.target("http://localhost:8080")
                .path("api").path("killme");

        // suspend process
        Response response = killme.request().get();
        Assert.assertEquals(200, response.getStatus());

        client.close();
        Thread.sleep(150);

        client = ClientBuilder.newClient(); // new connection

        // verify that it yields 503
        WebTarget greeting = client.target("http://localhost:8080")
                       .path("api").path("greeting");


        response = greeting.request().get();
        Assert.assertEquals(503, response.getStatus());
    }
}
