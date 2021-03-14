package com.shalaby.poller.repo;

import com.shalaby.poller.model.Service;
import io.vertx.core.Future;
import io.vertx.ext.sql.ResultSet;

public interface ServiceRepository {
  Future<ResultSet> persistService(Service service);

  Future<ResultSet> getAllServices();

  Future<Void> updateServiceById(Integer serviceId, String newStatus, String lastChanged);

  Future<ResultSet> getServiceByUrl(String url);

}
