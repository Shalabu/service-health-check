package com.shalaby.poller.service;

import com.shalaby.poller.model.Service;
import com.shalaby.poller.model.ServiceStatus;
import com.shalaby.poller.persistance.DatabaseConnector;
import com.shalaby.poller.repo.ServiceRepository;
import com.shalaby.poller.repo.ServiceRepositoryImpl;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.sql.ResultSet;

import java.time.LocalDateTime;

public class ServiceService {
  private ServiceRepository serviceRepository;

  public ServiceService(Vertx vertx){
    this.serviceRepository = new ServiceRepositoryImpl(
        DatabaseConnector.getInstance(vertx).getSqlClient());
  }
  public ServiceService(ServiceRepository serviceRepository) {
    this.serviceRepository = serviceRepository;
  }

  public Future<ResultSet> createService(String name, String url){
    Service newService = new Service(name, url);
    String now = LocalDateTime.now().toString();
    newService.setAdded(now);
    newService.setLastChanged(now);
    newService.setStatus(ServiceStatus.UNKNOWN);
    return serviceRepository.persistService(newService);
  }

  public Future<ResultSet> getAllServices() {
  return serviceRepository.getAllServices();
  }

  public Future<Void> updateServiceStatus(Integer serviceId, String newStatus, String updated){
    return serviceRepository.updateServiceById(serviceId, newStatus, updated);
  }

  public Future<ResultSet> getServiceByUrl(String url){
    return serviceRepository.getServiceByUrl(url);
  }

  public ServiceRepository getServiceRepository() {
    return serviceRepository;
  }

  public void setServiceRepository(ServiceRepository serviceRepository) {
    this.serviceRepository = serviceRepository;
  }
}
