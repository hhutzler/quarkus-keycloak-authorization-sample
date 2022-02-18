# Using Keycloak Authorization Services and Policy Enforcer to Protect JAX-RS Applications

In this example, we build a very simple microservice which offers one endpoints:

* `/accounts`

These endpoints are protected and can only be accessed if a client is sending a bearer token along with the request, which must be valid (e.g.: signature, expiration and audience) and trusted by the microservice.
The HTTP POST with path /accounts creates an new account. It  can only run by users with admin role.
The HTTP GET with path /accounts can view account. Users with the agent role can view accounts.

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

## Starting and Configuring the Keycloak Server

To start a Keycloak Server you can use Docker and just run the following command: ( You may need to change the shared directory location !)
Note : The full database  export can be found at :  imports/full-db-export.json
$ docker run --name keycloak -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -p 8280:8080 -p 8543:8443 \
  -v "D:/dev/Quarkus/testing/keycloak-authorization-sample/imports:/opt/jboss/keycloak/imports" \
  quay.io/keycloak/keycloak:15.0.2`

Connect to the the keycloack container and run full database import

bash-4.4$ /opt/jboss/keycloak/bin/standalone.sh -Djboss.socket.binding.port-offset=100 \
  -Dkeycloak.migration.action=import -Dkeycloak.migration.provider=singleFile \
  -Dkeycloak.migration.file=/opt/jboss/keycloak/imports/full-db-export.json \
  -Dkeycloak.migration.strategy=OVERWRITE_EXISTING  -Dkeycloak.profile.feature.upload_scripts=enabled

For setup details read: [ https://www.helikube.de/keycloak-authorization-service-rbac/ ]

You should be able to access your Keycloak Server at http://localhost:8180/auth or https://localhost:8543/auth.

Log in as the `admin` user to access the Keycloak Administration Console.
Username should be `admin` and password `admin`.


## Building the application
:warning: **NOTE**: Wo do not want that Keycloak server will launch a container for us  ( happens whem  the application runs in dev mode !! ) 

This mean we start the Quarkus app in production mode !

$ mvn  compile quarkus:dev



Launch the Maven build on the checked out sources of this demo:
```bash
 ./mvnw package
```
## Starting and Configuring the Keycloak Server

:warning: **NOTE**: Do not start the Keycloak server when you run the application in a dev mode - `Dev Services for Keycloak` will launch a container for you.

### Live coding with Quarkus

The Maven Quarkus plugin provides a development mode that supports
live coding. To try this out:
```bash
./mvnw quarkus:dev
```
This command will leave Quarkus running in the foreground listening on port 8080.

Now open [OpenId Connect Dev UI](http://localhost:8080/q/dev). You will be asked to login into a _Single Page Application_. Log in as `alice:alice` - accessing the `/api/admin` will return `403` and `/api/users/me` - `200` as `alice` only has a _User Permission_ to access the `/api/users/me` resource. Logout and login as `admin:admin` - accessing both `/api/admin` and `/api/users/me` will return `200` since `admin` has both _Admin Permission_ to access the `/api/admin` resource and _User Permission_ to access the `/api/users/me` resource.

### Run Quarkus in JVM mode

When you're done iterating in developer mode, you can run the application as a
conventional jar file. First compile it:
```bash
./mvnw package
```
Then run it:

> java -jar ./target/quarkus-app/quarkus-run.jar

Have a look at how fast it boots, or measure the total native memory consumption.

### Run Quarkus as a native executable

You can also create a native executable from this application without making any
source code changes. A native executable removes the dependency on the JVM:
everything needed to run the application on the target platform is included in 
the executable, allowing the application to run with minimal resource overhead.

Compiling a native executable takes a bit longer, as GraalVM performs additional
steps to remove unnecessary codepaths. Use the  `native` profile to compile a
native executable:

> ./mvnw package -Dnative

After getting a cup of coffee, you'll be able to run this executable directly:

> ./target/security-keycloak-authorization-quickstart-1.0.0-SNAPSHOT-runner

### Testing the application

See the `Live coding with Quarkus` section above about testing your application in a dev mode.

You can test the application launched in JVM or Native modes with `curl`.

The application is using bearer token authorization and the first thing to do is obtain an access token from the Keycloak Server in
order to access the application resources:

```bash
export access_token=$(\
    curl --insecure -X POST https://localhost:8543/auth/realms/quarkus/protocol/openid-connect/token \
    --user backend-service:secret \
    -H 'content-type: application/x-www-form-urlencoded' \
    -d 'username=alice&password=alice&grant_type=password' | jq --raw-output '.access_token' \
 )
```

The example above obtains an access token for user `alice`.

Any user is allowed to access the
`http://localhost:8080/api/users/me` endpoint
which basically returns a JSON payload with details about the user.

```bash
curl -v -X GET \
  http://localhost:8080/api/users/me \
  -H "Authorization: Bearer "$access_token
```

The `http://localhost:8080/api/admin` endpoint can only be accessed by users with the `admin` role. If you try to access this endpoint with the
 previously issued access token, you should get a `403` response
 from the server.

```bash
curl -v -X GET \
   http://localhost:8080/api/admin \
   -H "Authorization: Bearer "$access_token
```

In order to access the admin endpoint you should obtain a token for the `admin` user:

```bash
export access_token=$(\
    curl --insecure -X POST https://localhost:8543/auth/realms/quarkus/protocol/openid-connect/token \
    --user backend-service:secret \
    -H 'content-type: application/x-www-form-urlencoded' \
    -d 'username=admin&password=admin&grant_type=password' | jq --raw-output '.access_token' \
 )
```

Please also note the integration tests depend on `Dev Services for Keycloak`.
