package org.obsidiantoaster.quickstart;

/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.wildfly.swarm.health.Health;
import org.wildfly.swarm.health.HealthStatus;

/**
 * @author Heiko Braun
 */
@Path("/")
@ApplicationScoped
public class HelloServiceController {

	static final String NAME_SERVICE_URL = "http://name-service";

	@GET
	@Path("/greeting")
	@Produces("application/json")
	public String getGreeting() {
		return String.format("Hello %s!", getName());
	}

	private String getName() {
		return requestName().readEntity(String.class);
	}


	@GET
	@Path("/check")
	@Health
	public HealthStatus checkNameService() {
		if (isNameServiceUp()) {
			return HealthStatus.named("name-service-check").up();
		}

		return HealthStatus.named("name-service-check")
				.withAttribute("name-service", "Name service doesn't function correctly")
				.down();
	}

	private boolean isNameServiceUp() {
		return requestName().getStatus() == 200;
	}

	private Response requestName() {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(NAME_SERVICE_URL);
		return target.request(MediaType.MEDIA_TYPE_WILDCARD).get();
	}


}
