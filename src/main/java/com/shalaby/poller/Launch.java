package com.shalaby.poller;

import io.vertx.core.Vertx;

public class Launch {
  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new MainVerticle());
  }
}
