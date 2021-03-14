package com.shalaby.poller.exceptions;

public class InvalidUrlException extends Exception{

  public InvalidUrlException(){
    super("Url is invalid");
  }
}
