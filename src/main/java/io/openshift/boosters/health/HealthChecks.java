package io.openshift.boosters.health;

import java.net.InetAddress;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.health.Health;
import org.wildfly.swarm.health.HealthStatus;

/**
 * A simple health check that verifies the server suspend state. It correspond to the /killme operation.
 *
 * @author Heiko Braun
 * @since 04/04/2017
 */
@Path("/service")
public class HealthChecks {

    @GET
    @Health
    @Path("/health")
    public HealthStatus check() {
        try {
            ModelNode op = new ModelNode();
            op.get("address").setEmptyList();
            op.get("operation").set("read-attribute");
            op.get("name").set("suspend-state");

            ModelControllerClient client = ModelControllerClient.Factory.create(InetAddress.getByName("localhost"), 9990);
            ModelNode response = client.execute(op);

            if(response.has("failure-description")) {
                throw new Exception(response.get("failure-description").asString());
            }

            boolean isRunning = response.get("result").asString().equals("RUNNING");
            if(isRunning) {
                return HealthStatus.named("server-state").up();
            }
            else {
                return HealthStatus.named("server-state").down();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
