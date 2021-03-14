package com.shalaby.poller;

import com.shalaby.poller.exceptions.InvalidUrlException;
import com.shalaby.poller.persistance.DatabaseConnector;
import com.shalaby.poller.poller.AsyncPoller;
import com.shalaby.poller.repo.ServiceRepository;
import com.shalaby.poller.repo.ServiceRepositoryImpl;
import com.shalaby.poller.service.ServiceService;
import com.shalaby.poller.service.ServiceUpdateWorker;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.commons.collections4.CollectionUtils;
import com.shalaby.poller.util.Validator;

import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.vertx.reactivex.core.http.HttpHeaders.*;

public class MainVerticle extends AbstractVerticle {
  private static final String FRONTEND_URL = "http://localhost:3000";
  Logger logger = LoggerFactory.getLogger(MainVerticle.class);
  ServiceService serviceService;
  ServiceRepository serviceRepository;
  public MainVerticle(){

  }
  public MainVerticle(Vertx vertx, ServiceService serviceService, ServiceRepository serviceRepository) {
    this.vertx = vertx;
    this.serviceService = serviceService;
    this.serviceRepository = serviceRepository;

  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);
    if (this.serviceService == null) {
      this.serviceRepository = new ServiceRepositoryImpl(
        DatabaseConnector.getInstance(vertx).getSqlClient());
      this.serviceService = new ServiceService(serviceRepository);
    }
    addWorkerVerticle();
    setRoutes(router);
    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(8080, result -> {
        if (result.succeeded()) {
          logger.info("Health check server started");
        } else {
          logger.warn("Health check server failed, " + result.cause());
        }
      });
  }

  private void addWorkerVerticle() {
    DeploymentOptions workerOpts = new DeploymentOptions().setWorker(true);
    vertx.deployVerticle(ServiceUpdateWorker.class.getName(), workerOpts);
    vertx.deployVerticle(AsyncPoller.class.getName(), workerOpts);
  }

  private void setRoutes(Router router) {
    router.route().handler(BodyHandler.create());
    router.route().handler(StaticHandler.create());
    router.get("/service").handler(this::getAllServices);
    router.post("/service").handler(this::addService);
  }


  private void getAllServices(RoutingContext req) {
    serviceService.getAllServices().onComplete(event -> {
      if (event.succeeded()) {
        req.response()
          .putHeader(CONTENT_TYPE, APPLICATION_JSON)
          .putHeader(ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND_URL)
          .putHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
          .setStatusCode(200)
          .end(Json.encodePrettily(event.result().getRows()));
      } else {
        req.response()
          .setStatusCode(503)
          .putHeader(ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND_URL)
          .putHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
          .end(event.cause().getMessage());
      }
    });

  }

  private void addService(RoutingContext req) {
    JsonObject jsonBody = req.getBodyAsJson();
    try {
      String serviceUrl = jsonBody.getString("url");
      String serviceName = jsonBody.getString("name");
      if (!Validator.validateUrl(serviceUrl))
        throw new InvalidUrlException();

      // check if URL already exists
      serviceService.getServiceByUrl(serviceUrl).onComplete(event -> {
        if (event.succeeded()) {
          // url not found, then persist the new service
          if (event.result() == null || CollectionUtils.isEmpty(event.result().getResults())) {
            serviceService.createService(serviceName, serviceUrl).onComplete(event1 -> {
              if (event.succeeded())
                // reply with CREATED
                reply(req, 201);
              else {
                logger.warn(event.cause());
                // reply with Error
                reply(req, 503);
              }
            });
          } else {
            // reply with DUPLICATE RESOURCE
            reply(req, 409);
          }
        }
      });

    } catch (InvalidUrlException e) {
      logger.warn(e.getMessage());
      // reply with BAD REQUEST
      reply(req, 400);
    }
  }

  private void reply(RoutingContext req, int statusCode) {
    req.response()
      .putHeader(CONTENT_TYPE, APPLICATION_JSON)
      .putHeader(ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND_URL)
      .putHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
      .setStatusCode(statusCode)
      .end();
  }
}
