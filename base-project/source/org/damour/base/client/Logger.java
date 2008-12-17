package org.damour.base.client;

public class Logger {
  public static final boolean DEBUG = true;

  public static void log(Throwable throwable) {
    if (DEBUG) {
      throwable.printStackTrace(System.err);
    }
  }  
  
  public static void log(String message) {
    if (DEBUG) {
      System.out.println(message);
    }
  }
}
