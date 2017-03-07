### Description

This QuickStart demonstrates the health check feature using two Wildfly Swarm microservices.
The `name-service` exposes a REST endpoint which returns a name as a String when it is called while the `hello-service` exposes
another REST endpoint which returns a hello message to the user.

Before to reply to the user it will call the `name-service` in order to get the name to be returned within the Hello World Message.

To control if the `name-service` is still alive, the `hello-service` implements the Health Check procedure to verify the status of the service

```
	private boolean isNameServiceUp() {
  		Optional<Response> response = requestName();
  		if(response.isPresent())
  			return response.get().getStatus() == 200;
  		else
  			return false;
  }
```

If `name-service` is alive, it will get as HTTP response the status `200` and the Health Status 
reported by the Wildfly Swarm health procedure will be `Up`
  
```
if (isNameServiceUp()) {
			return HealthStatus.named("name-service-check").up();
}

return HealthStatus.named("name-service-check")
    .withAttribute("name-service", "Name service doesn't function correctly")
    .down();
```
  
If now the `name-service` is down and unreachable, then the `hello-service` will be informed about this situation due to a regular heartbeat 
and will become unhealthy as the Health Status reported is `DOWN`.
 
As Openshift probes the service to control its Health status, it will discover that the health status of the `hello-service` is now `DOWN`
and will make it unavailable. So, every call issued to the `hello-service` through the Openshift service will receive a HTTP status 503 (service unavailable).

When the `name-service` will be restored (example : new pod created) and that `hello-service` will discover that it is alive again, then its health check status
will be changed to Up and OpenShift will allow to access it again.

### Usage

1. Deploy microservices with Fabric8 maven plugin:

    mvn clean fabric8:deploy -Popenshift

2. Open OpenShift console and navigate to your project's overview page.

3. Wait until both services are running.

4. Scale down `name-service` to `0` pod. Then the `hello-service` probe will start to fail and OpenShift will make this service unavailable from outside.

5. If you'd try to call the `hello-service` route, you should get an HTTP error 503 service unavailable.

6. Scale up `name-service` to 1 pod. Soon, you will see that the `hello-service` probe will start again and OpenShift will make this service available again.
