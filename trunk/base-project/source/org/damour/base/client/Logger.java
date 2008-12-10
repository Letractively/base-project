package org.damour.base.client;

public class Logger {
  public static final boolean DEBUG = true;

  public static void log(String message) {
    if (DEBUG) {
      System.out.println(message);
    }
  }
}
