package org.damour.base.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class Logger {

  public static boolean DEBUG = false;

  private static String logName = null;
  private static DateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static DateFormat logFileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

  static {
    try {
      File tmpDir = new File(BaseSystem.getTempDir());
      tmpDir.mkdirs();
    } catch (Throwable t) {
    }
  }

  private static String getLogName() {
    if (logName == null) {
      logName = BaseSystem.getTempDir() + logFileDateFormat.format(new Date()) + ".log.txt";
      DEBUG = "true".equalsIgnoreCase((String) BaseSystem.getSettings().get("debug"));
    }
    return logName;
  }

  public static String convertThrowableToString(Throwable t) {
    String trace = null;
    try {
      StringWriter sw = new StringWriter();
      PrintWriter outWriter = new PrintWriter(sw);
      t.printStackTrace(outWriter);
      trace = sw.toString();
      sw.close();
    } catch (Throwable never) {
    }
    return trace;
  }

  public static String convertStringToHTML(final String input) {
    return input.replaceAll("\n", "<BR/>\n").replaceAll(" ", "&nbsp;").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
  }

  public synchronized static void log(Throwable throwable) {
    if (DEBUG) {
      throwable.printStackTrace(System.err);
    }
    try {
      FileOutputStream logOut = new FileOutputStream(getLogName(), true);
      logOut.write(logDateFormat.format(new Date()).getBytes());
      logOut.write(" ".getBytes());
      logOut.write(convertThrowableToString(throwable).getBytes());
      logOut.write("\n".getBytes());
      logOut.close();
    } catch (Throwable t) {
    }
  }

  public synchronized static void log(String message) {
    if (DEBUG) {
      System.out.println(message);
    }
    try {
      FileOutputStream logOut = new FileOutputStream(getLogName(), true);
      logOut.write(logDateFormat.format(new Date()).getBytes());
      logOut.write(" ".getBytes());
      logOut.write(message.getBytes());
      logOut.write("\n".getBytes());
      logOut.close();
    } catch (Throwable t) {
    }
  }

  public static void resetLogger() {
    logName = null;
  }

  public static void dump(Properties properties) {
    if (properties == null) {
      return;
    }
    try {
      log("properties {");
      for (Object key : properties.keySet()) {
        Object value = properties.get(key);
        log("  " + key + " = " + value);
      }
      log("}");
    } catch (Throwable t) {
    }
  }

}
