package com.shalaby.poller.persistance;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;

public class DatabaseConnector {
  private static final String DB_PATH = "poller.db";
  private static DatabaseConnector instance;
  private final SQLClient sqlClient;

  private DatabaseConnector(Vertx vertx) {
    JsonObject config = new JsonObject()
      .put("url", "jdbc:sqlite:" + DB_PATH)
      .put("driver_class", "org.sqlite.JDBC")
      .put("max_pool_size", 30);

    sqlClient = JDBCClient.createShared(vertx, config);
  }

  public static DatabaseConnector getInstance(Vertx vertx) {
    if (instance == null) {
      instance = new DatabaseConnector(vertx);
    }
    return instance;
  }


  public Promise<ResultSet> executeQuery(String query, JsonArray params) {

    if(!query.endsWith(";")) {
      query = query + ";";
    }
    Promise<ResultSet> resultSetPromise = Promise.promise();
    if (params != null){
      sqlClient.queryWithParams(query, params, event -> {
        if (event.failed()) {
          resultSetPromise.fail(event.cause());
        } else {
          resultSetPromise.tryComplete(event.result());
        }

      });}
    else {

      sqlClient.query(query, event -> {
        if (event.failed()) {
          resultSetPromise.fail(event.cause());
        } else {
          resultSetPromise.tryComplete(event.result());
        }

      });

    }
    return resultSetPromise;
  }

  public Future<ResultSet> query(String query) {
    if (query == null || query.isBlank())
      return Future.failedFuture("Empty query provided");
    return executeQuery(query, null).future();
  }

  public Future<ResultSet> query(String query, JsonArray params) {
    if (query == null || query.isBlank())
      return Future.failedFuture("Empty query provided");
    return executeQuery(query,params).future();
  }
  public SQLClient getSqlClient(){
    return this.sqlClient;
  }
}
