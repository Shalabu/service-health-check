package com.shalaby.poller.model;


public class Service {
  private String name;
  private String url;
  private ServiceStatus status;
  private String added;
  private String lastChanged;

  public Service() {

  }

  public Service(String name, String url) {
    this.name = name;
    this.url = url;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public ServiceStatus getStatus() {
    return status;
  }

  public void setStatus(ServiceStatus status) {
    this.status = status;
  }

  public String getAdded() {
    return added;
  }

  public void setAdded(String added) {
    this.added = added;
  }

  public String getLastChanged() {
    return lastChanged;
  }

  public void setLastChanged(String lastChanged) {
    this.lastChanged = lastChanged;
  }

  @Override
  public String toString() {
    return "Service{" +
      "name='" + name + '\'' +
      ", url='" + url + '\'' +
      ", status=" + status +
      ", added='" + added + '\'' +
      ", lastChanged='" + lastChanged + '\'' +
      '}';
  }
}
