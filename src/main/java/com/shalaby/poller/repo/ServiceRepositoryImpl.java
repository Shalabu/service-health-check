package com.shalaby.poller.repo;

import com.shalaby.poller.model.Service;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;

public class ServiceRepositoryImpl implements ServiceRepository{
  String insertQuery = "INSERT INTO service ('name','url','status', 'created', 'updated') VALUES (?, ?, ?, ?, ?);";
  String updateQuery = "UPDATE service SET status = ?, updated = ? WHERE id = ?;";
  String selectAllQuery = "SELECT * FROM service;";
  String getByUrl = "SELECT 1 FROM service WHERE url = ?;";
  Logger logger = LoggerFactory.getLogger(ServiceRepositoryImpl.class);
  private SQLClient sqlClient;
  public ServiceRepositoryImpl(SQLClient client){
    this.sqlClient = client;
  }

  @Override
  public Future<ResultSet> persistService(Service service) {
    JsonArray params = new JsonArray()
      .add(service.getName())
      .add(service.getUrl())
      .add(service.getStatus().toString())
      .add(service.getAdded())
      .add(service.getLastChanged());
    Promise<ResultSet> servicePromise = Promise.promise();
    sqlClient.queryWithParams(insertQuery, params, event -> {
      if (event.failed()) {
        logger.warn(event.cause());
        servicePromise.tryFail(event.cause());
      } else {
        servicePromise.tryComplete();
      }
    });
    return servicePromise.future();
  }

  @Override
  public Future<ResultSet> getAllServices() {
    Promise<ResultSet> servicePromise = Promise.promise();
    sqlClient.query(selectAllQuery, event -> {
      if (event.failed()) {
        logger.warn(event.cause());
        servicePromise.tryFail(event.cause());
      } else {
        servicePromise.tryComplete(event.result());
      }
    });
    return servicePromise.future();
  }

  @Override
  public Future<Void> updateServiceById(Integer serviceId, String newStatus, String lastChanged) {
    Promise<Void> servicePromise = Promise.promise();
    JsonArray params = new JsonArray()
      .add(newStatus)
      .add(lastChanged)
      .add(serviceId);
    sqlClient.queryWithParams(updateQuery, params, event -> {
      if (event.failed()) {
        logger.warn(event.cause());
      } else {
        servicePromise.complete();
      }
    });
    return servicePromise.future();
  }

  @Override
  public Future<ResultSet> getServiceByUrl(String url) {
    Promise<ResultSet> servicePromise = Promise.promise();
    JsonArray params = new JsonArray()
      .add(url);
    sqlClient.queryWithParams(getByUrl, params, event -> {
      if (event.failed()) {
        logger.warn(event.cause());
        servicePromise.tryFail(event.cause());
      } else {
        servicePromise.tryComplete(event.result());
      }
    });
    return servicePromise.future();
  }

}
