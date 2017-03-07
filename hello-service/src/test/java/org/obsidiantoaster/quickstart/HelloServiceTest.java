package org.obsidiantoaster.quickstart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

/**
 * @author Heiko Braun
 */
@RunWith(Arquillian.class)
@DefaultDeployment
public class HelloServiceTest {

    @Test
    @RunAsClient
    public void test_health_check() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080/check");

        Response response = target.request(MediaType.MEDIA_TYPE_WILDCARD).get();
        Assert.assertEquals(503, response.getStatus()); // service down
        Assert.assertTrue(response.readEntity(String.class).contains("DOWN"));
    }

    @Test
    @RunAsClient
    public void test_service_invocation() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080/greeting");

        Response response = target.request(MediaType.MEDIA_TYPE_WILDCARD).get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.readEntity(String.class).contains("Failure"));
    }

}

