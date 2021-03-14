package com.shalaby.poller.service;

import com.shalaby.poller.MainVerticle;
import com.shalaby.poller.repo.ServiceRepository;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(VertxExtension.class)
@RunWith(MockitoJUnitRunner.class)
class TestMainVerticle {
  static ServiceRepository serviceRepositoryMock;
   static ServiceService serviceService;
  static MainVerticle mainVerticle;
  static ResultSet resultSet;

  @BeforeAll
  public static void setup() {
    resultSet = prepareResultSet();
    serviceRepositoryMock = Mockito.mock(ServiceRepository.class);
    serviceService = new ServiceService(serviceRepositoryMock);
  }

  @Test
  @DisplayName("When application starts, a server on localhost and port 8080 should start" +
    "a list of services should return from /service endpoint wit status code 200")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void get_all_services(Vertx vertx, VertxTestContext testContext) throws IOException, URISyntaxException {
    Mockito.when(serviceRepositoryMock.getAllServices())
      .thenReturn(Future.succeededFuture(resultSet));
    mainVerticle = new MainVerticle(vertx, serviceService, serviceRepositoryMock);
    vertx.deployVerticle(mainVerticle);
    WebClient webClient = WebClient.create(vertx);
    webClient.get(8080, "::1", "/service")
      .send(response -> testContext.verify(() -> {
        assertEquals(200, response.result().statusCode());
        assertEquals(resultSet.getRows().get(0), response.result().bodyAsJsonArray().getJsonObject(0));
        testContext.completeNow();
      }));
  }


  @Test
  @DisplayName("When service is added, a response with 201 (created) should be returned")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void add_service_success(Vertx vertx, VertxTestContext testContext) {
    JsonObject object = new JsonObject()
      .put("url", "https://www.example.com")
      .put("name", "Example");
    Mockito.when(serviceRepositoryMock.persistService(any()))
      .thenReturn(Future.succeededFuture(resultSet));
    Mockito.when(serviceRepositoryMock.getServiceByUrl(any())).thenReturn(Future.succeededFuture(new ResultSet()));
    mainVerticle = new MainVerticle(vertx, serviceService, serviceRepositoryMock);
    vertx.deployVerticle(mainVerticle);
    WebClient webClient = WebClient.create(vertx);
      webClient.post(8080, "::1", "/service")
      .putHeader("Content-Type", "application/json")
      .putHeader("Content-Length", Integer.toString(object.toString().length()))
      .sendJson(object, response -> testContext.verify(() -> {
        assertEquals(201, response.result().statusCode());
        testContext.completeNow();
      }));
  }

  @Test
  @DisplayName("When service is added with invalid url, a response with 400 (bad request) should be returned")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void add_service_invalid_url(Vertx vertx, VertxTestContext testContext) {
    JsonObject object = new JsonObject()
      .put("url", "invaliurlhere")
      .put("name", "Example");
    Mockito.when(serviceRepositoryMock.getServiceByUrl(any())).thenReturn(Future.succeededFuture(new ResultSet()));
    mainVerticle = new MainVerticle(vertx, serviceService, serviceRepositoryMock);
    vertx.deployVerticle(mainVerticle);
    WebClient webClient = WebClient.create(vertx);
    webClient.post(8080, "::1", "/service")
      .putHeader("Content-Type", "application/json")
      .putHeader("Content-Length", Integer.toString(object.toString().length()))
      .sendJson(object, response -> testContext.verify(() -> {
        assertEquals(400, response.result().statusCode());
        testContext.completeNow();
      }));
  }

  @Test
  @DisplayName("When service is added with duplicate url, a response with 409 (conflict) should be returned")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void add_service_duplicate_url(Vertx vertx, VertxTestContext testContext) {
    JsonObject object = new JsonObject()
      .put("url", "https://www.example.com")
      .put("name", "Example");
    Mockito.when(serviceRepositoryMock.persistService(any()))
      .thenReturn(Future.succeededFuture(resultSet));
    Mockito.when(serviceRepositoryMock.getServiceByUrl(any())).thenReturn(Future.succeededFuture(resultSet));
    mainVerticle = new MainVerticle(vertx, serviceService, serviceRepositoryMock);
    vertx.deployVerticle(mainVerticle);
    WebClient webClient = WebClient.create(vertx);
    webClient.post(8080, "::1", "/service")
      .putHeader("Content-Type", "application/json")
      .putHeader("Content-Length", Integer.toString(object.toString().length()))
      .sendJson(object, response -> testContext.verify(() -> {
        assertEquals(409, response.result().statusCode());
        testContext.completeNow();
      }));
  }

  private static ResultSet prepareResultSet(){
    List<String> columns = List.of("id", "name", "url", "status", "created", "updated");
    ResultSet resultSet = new ResultSet();
    resultSet.setColumnNames(columns);
    JsonArray jsonArray = new JsonArray();
    jsonArray.add(1);
    jsonArray.add("Example");
    jsonArray.add("https://www.example.com");
    jsonArray.add("OK");
    jsonArray.add("2021-03-12T13:51:34.609049");
    jsonArray.add("2021-03-12T13:51:34.609049");

    resultSet.setResults(List.of(jsonArray));
    return resultSet;
  }
}
