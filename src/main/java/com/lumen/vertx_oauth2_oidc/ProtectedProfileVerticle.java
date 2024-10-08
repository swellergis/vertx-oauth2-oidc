package com.lumen.vertx_oauth2_oidc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.providers.GithubAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine;

public class ProtectedProfileVerticle extends AbstractVerticle {

    private String bindAddress;
    private int bindPort;

    private static final String CLIENT_ID = System.getenv("GITHUB_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("GITHUB_CLIENT_SECRET");

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        readConfigProps();

        HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

        Router router = Router.router(vertx);

        router.get("/").handler(ctx -> {
            // we pass the client id to the template
            ctx.put("client_id", CLIENT_ID);
            // and now delegate to the engine to render it.
            engine.render(ctx.data(), "views/index.hbs").onSuccess(buffer -> {
                ctx.response().putHeader("Content-Type", "text/html").end(buffer);
            }).onFailure(ctx::fail);
        });

        OAuth2Auth authProvider = GithubAuth.create(vertx, CLIENT_ID, CLIENT_SECRET);

        router.get("/protected").handler(
                // OAuth2AuthHandler.create(vertx, authProvider, "http://localhost:8080/callback")
                OAuth2AuthHandler
                        .create(vertx, authProvider, "https://vertx.local/callback")
                        .setupCallback(router.route("/callback"))
                        .withScope("user:email"))
                .handler(new ProtectedProfileHandler(authProvider, engine));

        vertx.createHttpServer().requestHandler(router).listen(bindPort, bindAddress)
                .onSuccess(server -> {
                    System.out.println(
                            "HTTP server started on port: " + server.actualPort());
                    startPromise.complete();
                }).onFailure(startPromise::fail);
    }

    private void readConfigProps() throws IllegalArgumentException, InterruptedException {
        bindAddress = "0.0.0.0";
        bindPort = 8080;
    }

}
