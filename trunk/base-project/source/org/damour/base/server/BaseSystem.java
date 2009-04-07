package org.damour.base.server;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

public class BaseSystem {

  private static final long startupDate = System.currentTimeMillis();
  private static String domainName = "" + startupDate;
  private static boolean isDomainNameSet = false;
  private static ClassLoader classLoader = null;

  static {
    try {
      setBaseClassLoader(BaseSystem.class.getClassLoader());
    } catch (Throwable t) {
      Logger.log(t);
      try {
        BaseSystem bs = new BaseSystem();
        setBaseClassLoader(bs.getClass().getClassLoader());
      } catch (Throwable tt) {
        Logger.log(tt);
      }
    }
  }

  public static String getDomainName() {
    return domainName;
  }

  public static String getDomainName(HttpServletRequest request) {
    if (!isDomainNameSet) {
      isDomainNameSet = true;
      domainName = request.getServerName();
      if (domainName.lastIndexOf(".") > domainName.indexOf(".")) {
        // remove subdomain
        domainName = domainName.substring(domainName.indexOf(".") + 1);
      }
    }
    return domainName;
  }

  public static void setDomainName(String domainName) {
    BaseSystem.domainName = domainName;
  }

  public static String getTempDir() {
    return File.separatorChar + "tmp" + File.separatorChar + BaseSystem.getDomainName() + File.separatorChar;
  }

  public static long getStartupDate() {
    return startupDate;
  }

  public static void setBaseClassLoader(final ClassLoader classLoader) {
    if (BaseSystem.classLoader != classLoader) {
      Logger.log("BaseSystem:setBaseClassLoader = " + classLoader);
      Logger.log(new Exception("Debug Stack Dump"));
      BaseSystem.classLoader = classLoader;
    }
  }

  public static ClassLoader getBaseClassLoader() {
    return classLoader;
  }

}
