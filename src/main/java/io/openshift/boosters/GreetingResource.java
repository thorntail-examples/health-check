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

import java.net.InetAddress;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

@Path("/")
public class GreetingResource {

    private static final String template = "Hello, %s!";

    @GET
    @Path("/greeting")
    @Produces("application/json")
    public Greeting greeting(@QueryParam("name") String name) {
        String suffix = name != null ? name : "World";
        return new Greeting(String.format(template, suffix));
    }

    /**
     * The /stop operation is actually just going to suspend the server inbound traffic,
     * which leads to 503 when subsequent HTTP requests are received
     */
    @GET
    @Path("/stop")
    public Response stop() {
        ModelNode op = new ModelNode();
        op.get("address").setEmptyList();
        op.get("operation").set("suspend");

        try (ModelControllerClient client = ModelControllerClient.Factory.create(
                InetAddress.getByName("localhost"), 9990)) {
            ModelNode response = client.execute(op);

            if (response.has("failure-description")) {
                throw new Exception(response.get("failure-description").asString());
            }

            return Response.ok(response.get("result").asString()).build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
