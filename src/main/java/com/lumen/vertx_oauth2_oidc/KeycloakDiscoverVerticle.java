package com.lumen.vertx_oauth2_oidc;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.providers.OpenIDConnectAuth;
import io.vertx.ext.web.Router;

public class KeycloakDiscoverVerticle extends AbstractVerticle 
{
    private final static Logger LOG = Logger.getLogger(KeycloakDiscoverVerticle.class.getName());

    private String bindAddress;
    private int bindPort;

    private static final String BASE_URL = System.getenv("KEYCLOAK_BASE_URL");
    private static final String REALM = System.getenv("KEYCLOAK_REALM");
    private static final String CLIENT_ID = System.getenv("KEYCLOAK_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("KEYCLOAK_CLIENT_SECRET");

    @Override
    public void start(Promise<Void> startPromise) throws Exception 
    {
        LOG.log(Level.INFO, "Starting HTTP server");

        readConfigProps();

        // Configure routes
        Router router = initRoutes();

        // Create the HTTP server
        HttpServerOptions options = new HttpServerOptions();
        vertx.createHttpServer(options)
                // Handle every request using the router
                .requestHandler(router)
                // Start listening
                .listen(bindPort, bindAddress)
                // Print the port
                .onSuccess(server -> {
                    startPromise.complete();
                    LOG.log(Level.INFO,
                            String.format("HTTP server started on port %s", server.actualPort()));
                }).onFailure(event -> {
                    startPromise.fail(event);
                    LOG.log(Level.SEVERE,
                            String.format("Failed to start HTTP server:%s", event.getMessage()));
                });

    }

    private Router initRoutes()
    {
        // set up keycloak authentication
        final Future<OAuth2Auth> oa2 = OpenIDConnectAuth
                .discover(vertx,
                        new OAuth2Options().setClientId(CLIENT_ID).setTenant(REALM)
                                .setClientSecret(CLIENT_SECRET)
                                .setSite(String.format("%s/realms/%s", BASE_URL, REALM)))

                .onSuccess(oauth2 -> {
                    // the setup call succeeded.
                    // at this moment your auth is ready to use
                    LOG.log(Level.INFO, "OAuth2 setup call succeeded!!!");
                }).onFailure(err -> {
                    // the setup failed.
                    LOG.warning(
                            String.format("Initialization of OAuth2 failed: %s", err.getMessage()));
                });

        Router router = Router.router(vertx);

        // health check
        router.get("/").handler(ctx -> {
            ctx.response().end("howdy!");
        });

        return router;
    }

    private void readConfigProps() throws IllegalArgumentException, InterruptedException
    {
        bindAddress = "0.0.0.0";
        bindPort = 8081;
    }

}
