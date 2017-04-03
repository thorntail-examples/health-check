# Introduction

## Problem Setting

When an application is deployed top of OpenShift/Kubernetes it is important to figure out if each container is available and able to serve incoming requests. By implementing the health-check pattern, it becomes possible to monitor the health of the container and whether it is able to  serve traffic

## Description

The purpose of this use case is to demonstrate how the Kubernetes health checks work in order to control if a container is still alive (= liveness) and ready to serve (= readiness) the traffic for the application’s HTTP endpoints.

To demonstrate this behavior, we will configure an HTTP endpoint which is used by Kubernetes to issue HTTP requests. If the container is still alive, as the Health HTTP endpoint is able to reply, the management platform will receive 200 as return code and then no further action is required.

But, if the HTTP endpoint doesn’t return a response (JVM no longer running, thread blocked, etc), then the platform will kill the pod and recreate a new container to restart the application.

As the pod will be down for a certain period of time, we will be able to show that the endpoint exposing the service is no longer available; in this case, an HTTP 503 response will be returned. The user gets this return code from the Kubernetes proxy; the management platform has detected that the endpoint used to check if the container is ready to serve the traffic can’t reply. By consequence, the IP address and port of the server exposing the service will be removed from the Kubernetes proxy.

## Concepts & Architectural Patterns

Health Check using Liveness (= process is alive, jvm started) and Readiness (= ready to serve traffic) probes

# Build the project

The project uses WildflySwarm to create and package the service.

Execute the following maven command:

```
mvn clean install
```

# Launch and test

1. Run the following command to start the maven goal of WildFly Swarm:
```
mvn wildfly-swarm:run
```

2. If the application launched without error, use the following command to access the REST endpoint exposed using curl or httpie tool:

```
http http://localhost:8080/api/greeting
curl http://localhost:8080/api/greeting
```

It should return the value `Hello, World!`.

# Openshift Online

## Login and prepare your openshift account

1. Go to [OpenShift Online](https://console.dev-preview-int.openshift.com/console/command-line) to get the token used by the oc client for authentication and project access.

2. Using the `oc` client, execute the following command to replace MYTOKEN with the one from the Web Console:

    ```
    oc login https://api.dev-preview-int.openshift.com --token=MYTOKEN
    ```
3. To allow the WildFly Swarm application running as a pod to access the Kubernetes Api to retrieve the Config Map associated to the application name of the project `swarm-rest-configmap`,
   the view role must be assigned to the default service account in the current project:

    ```
    oc policy add-role-to-user view -n $(oc project -q) -z default
    ```      

## Experimenting with service states and health checks in an Openshift environment

1. Build the project prior to deploying to Openshift

  ```
  mvn clean install
  ```

2. Deploy microservices with Fabric8 maven plugin:

  ```
  mvn clean fabric8:deploy -Popenshift
  ```

3. Get the route url:

    ```
    oc get route/wildfly-swarm-health
    NAME              HOST/PORT                                          PATH      SERVICE                TERMINATION   LABELS
    wildfly-swarm-health   <HOST_PORT_ADDRESS>             wildfly-swarm-health:8080
    ```

4. Use the Host or Port address to access the service.

    ```
    curl http://<HOST_PORT_ADDRESS>/api/greeting    
    ```

    The service should respond with `Hello, World!`:

    ```
    {    
    "content": "Hello, World!"
	 }
    ```

5. Kill the service to simulate a failure state:

  ```
  curl http://<HOST_PORT_ADDRESS>/api/killme
  ```

  > Internally, this suspends the server, which means it will no longer accept HTTP requests, but respond with 503 across the board. But for the Openshift end, which does rely on HTTP probes to check the liveliness of the pod, this will signal an unhealthy state and the pod should be replaced with a new instance after a while.

6. Verify the pod is gone

  A subsequent request, shortly after the pod was killed, should return 503 on the route URL.

  ```
  curl http://<HOST_PORT_ADDRESS>/api/greeting    
  ```

7. If you move the Openshift console, you should be able to see the pod failover and new one being started after a while.

Congratulations, you just finished your first health check tutorial!
