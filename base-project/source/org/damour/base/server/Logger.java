package org.damour.base.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Logger {

  public static final boolean DEBUG = true;

  private static String logName = null;
  private static DateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static DateFormat logFileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

  static {
    try {
      File tmpDir = new File("/tmp");
      tmpDir.mkdirs();
    } catch (Throwable t) {
    }
  }
  
  public static String getLogName() {
    if (logName == null) {
      if (BaseSystem.getDomainName() == null) {
        logName = "/tmp/baseproject." + logFileDateFormat.format(new Date()) + ".log.txt";
      } else {
        logName = "/tmp/" + BaseSystem.getDomainName() + logFileDateFormat.format(new Date()) + ".log.txt";
      }
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

}
