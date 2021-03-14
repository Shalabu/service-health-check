package com.shalaby.poller.persistance;

import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class DatabaseCreation {
  private static final String CREATE_TABLE_SERVICE = "CREATE TABLE IF NOT EXISTS service ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' VARCHAR(128) NOT NULL," +
    " 'url' VARCHAR(128) NOT NULL UNIQUE, 'status' VARCHAR(128), 'created' TEXT, 'updated' TEXT)";

  private static final Logger logger = LoggerFactory.getLogger(DatabaseCreation.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    DatabaseConnector connector = DatabaseConnector.getInstance(vertx);
    connector.query(CREATE_TABLE_SERVICE).onComplete(event -> {
      if (event.succeeded()){
        logger.info("Database initialized");
      }
      else {
        logger.warn("Database creation failed: " + event.cause());
        System.exit(0);
      }

    });
  }
}
