package com.lumen.vertx_oauth2_oidc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    int port = 8080;
    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    }).listen(8080).onComplete(http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println(String.format("HTTP server started on port %d", port));
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
