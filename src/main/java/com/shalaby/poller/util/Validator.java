package com.shalaby.poller.util;
import java.net.URL;

public class Validator {

  public static boolean validateUrl(String url){
    try {
      new URL(url).toURI();
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }
}
