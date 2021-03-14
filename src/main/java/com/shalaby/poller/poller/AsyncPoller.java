package com.shalaby.poller.poller;

import com.shalaby.poller.persistance.DatabaseConnector;
import com.shalaby.poller.repo.ServiceRepository;
import com.shalaby.poller.repo.ServiceRepositoryImpl;
import com.shalaby.poller.service.ServiceService;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.time.LocalDateTime;

import static javax.management.timer.Timer.ONE_MINUTE;

public class AsyncPoller extends AbstractVerticle {
  public static final String ADDRESS = "service.feed";
  private final Logger logger = LoggerFactory.getLogger(AsyncPoller.class);
  private ServiceRepository serviceRepository;
  private ServiceService serviceService;
  EventBus eventBus;
  @Override
  public void start() throws Exception {
    serviceRepository = new ServiceRepositoryImpl(DatabaseConnector.getInstance(vertx).getSqlClient());
    serviceService = new ServiceService(serviceRepository);
    eventBus = vertx.eventBus();
    vertx.setPeriodic(ONE_MINUTE, timerId ->pollStatus());
  }

  private Future<Void> pollStatus() {
    Promise<Void> promise = Promise.promise();
    serviceService.getAllServices().onComplete(event -> {
        WebClient webClient = WebClient.create(vertx);
        if (event.succeeded()) {
          event.result().getRows().forEach(row -> {
            final String storedStatus = row.getString("status");
            final String url = row.getString("url");
            final int id = row.getInteger("id");
            webClient.get(url).send().onComplete(resp -> {
              final String updatedStatus = resp.result().statusMessage();
              // skip if not changed
              if (!storedStatus.equalsIgnoreCase(updatedStatus)) {
                String now = LocalDateTime.now().toString();
                // publish updates
                eventBus.publish(ADDRESS, new JsonObject().put("id", id).put("status", updatedStatus).put("updated", now));
              }
            });
          });
          promise.complete();
        } else {
          logger.warn("Poller service failed to check the latest status, " + event.cause());
          promise.fail(event.cause());
        }
      }
    );
    return promise.future();
  }
}
