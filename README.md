# Using Keycloak Authorization Services and Policy Enforcer to Protect JAX-RS Applications

In this example, we build a very simple microservice which offers one endpoints:

* `/accounts`

These endpoints are protected and can only be accessed if a client is sending a bearer token along with the request, which must be valid (e.g.: signature, expiration and audience) and trusted by the microservice.
The HTTP POST with path /accounts creates an new account. It  can only run by users with admin role.
The HTTP GET with path /accounts can view account. Users with the agent role or admin role can view accounts.

The bearer token is issued by a Keycloak Server and represents the subject to which the token was issued for.
For being an OAuth 2.0 Authorization Server, the token also references the client acting on behalf of the user.


This is a very simple example using RBAC policies to govern access to your resources.
However, Keycloak supports other types of policies that you can use to perform even more fine-grained access control.
By using this example, you'll see that your application is completely decoupled from your authorization policies with enforcement being purely based on the accessed resource.

## Requirements

To compile and run this demo you will need:

- JDK 11+
- Keycloak

### Configuring JDK 11+
Make sure that `JAVA_HOME` environment variables have been set, and that a JDK 11+ `java` command is on the path.

## Starting and Configuring the Keycloak Server ( see Part 1 )
- For Keycloak Setup read [Part 1: Keycloak Setup ](https://www.helikube.de/part-1-setup-for-keycloak-authorization-sample )

## Add. Info can be found at:
- For Quarkus Setup read  [Part 2: Quarkus Setup ]( https://www.helikube.de/part-2-running-fine-grained-keycloak-authorization-feature-with-quarkus/)
- For Angular App details read [Part 3: OIDC authorization Setup  ](https://www.helikube.de/part-3-running-an-odic-angular-app-to-test-keycloak-authorization-feature)


Log in as the `admin` user to access the Keycloak Administration Console.
Username should be `testadmin` and password `xxx` or `testuser` and password `xxx`


## Building the application
The Maven Quarkus plugin provides a development mode that supports
live coding. To try this out:
$ mvn  compile quarkus:dev


Now open [OpenId Connect Dev UI](http://localhost:8080/q/dev). You will be asked to login into a _Single Page Application_. 
- Log in as `testuser` - accessing the `/accounts` will return  `200`
- Log in as `testadmin` - accessing the `/accounts` will return  `200`

### Testing the application with curl 

Use script scripts/curl-test.sh 
$ ./curl-test.sh testadmin
   POST : User testadmin: Can create accounts !
   GET  : User testadmin: Can view accounts !

$ ./curl-test.sh testuser
  POST : User testuser: HTTP 403
  GET  : User testuser: Can view accounts !

### Testing the application with maven

$ mvn test
