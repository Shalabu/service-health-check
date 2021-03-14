package com.shalaby.poller.service;

import com.shalaby.poller.model.ServiceStatus;
import com.shalaby.poller.poller.AsyncPoller;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

public class ServiceUpdateWorker extends AbstractVerticle {
  ServiceService serviceService;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    serviceService = new ServiceService(vertx);
    // register to service status update events
    vertx.eventBus().consumer(AsyncPoller.ADDRESS).handler(update -> {
      int id = ((JsonObject)update.body()).getInteger("id");
      String newStatus = mapStatus(((JsonObject)update.body()).getString("status")).toString();
      String at = ((JsonObject)update.body()).getString("updated");
      serviceService.updateServiceStatus(id,newStatus,at);
    });
  }

  private ServiceStatus mapStatus(String status) {
    switch (status){
      case "ok":
      case "OK":
        return ServiceStatus.OK;
      default:
        return ServiceStatus.FAIL;
    }
  }
}
