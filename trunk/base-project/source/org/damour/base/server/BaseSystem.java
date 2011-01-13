package org.damour.base.server;

import java.io.File;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.damour.base.client.utils.StringUtils;
import org.damour.base.server.hibernate.HibernateUtil;

public class BaseSystem {

  public static final String DEFAULT_SMTP_HOST = "relay-hosting.secureserver.net";
  private static final long startupDate = System.currentTimeMillis();
  private static String domainName = "" + startupDate;
  private static boolean isDomainNameSet = false;
  private static ClassLoader classLoader = null;
  private static Properties settings = null;

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
      // if (domainName.lastIndexOf(".") > domainName.indexOf(".")) {
      // // remove subdomain
      // domainName = domainName.substring(domainName.indexOf(".") + 1);
      // }
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
      Logger.log("BaseSystem:setBaseClassLoader = " + classLoader.getClass().getName());
      BaseSystem.classLoader = classLoader;
    }
  }

  public static ClassLoader getBaseClassLoader() {
    if (classLoader == null) {
      return BaseSystem.class.getClassLoader();
    }
    return classLoader;
  }

  public static synchronized void reset() {
    settings = null;
    HibernateUtil.resetHibernate();
    Logger.resetLogger();
  }

  public static boolean requireAccountValidation() {
    return "true".equalsIgnoreCase(getSettings().getProperty("requireAccountValidation"));
  }

  private static IEmailService emailService;

  public static IEmailService getEmailService() {
    if (emailService == null) {
      try {
        emailService = (IEmailService) Class.forName(getSettings().getProperty("IEmailServiceImpl")).newInstance();
      } catch (Throwable t) {
        emailService = new EmailHelper();
        Logger.log(t);
      }
    }
    return emailService;
  }

  public static String getSmtpHost() {
    String smtpHost = getSettings().getProperty("smtpHost");
    if (StringUtils.isEmpty(smtpHost)) {
      smtpHost = DEFAULT_SMTP_HOST;
    }
    return smtpHost;
  }

  public static String getAdminEmailAddress() {
    String adminEmailAddress = getSettings().getProperty("adminEmailAddress");
    if (StringUtils.isEmpty(adminEmailAddress)) {
      adminEmailAddress = "admin@" + getDomainName();
    }
    return adminEmailAddress;
  }

  public static synchronized Properties getSettings() {
    if (settings != null) {
      return settings;
    }
    settings = new Properties();
    try {
      settings.load(BaseSystem.getBaseClassLoader().getResourceAsStream("settings.properties"));
      Logger.dump(settings);
    } catch (Throwable t) {
      Logger.log(t);
      try {
        settings.load(BaseSystem.class.getClassLoader().getResourceAsStream("settings.properties"));
        Logger.dump(settings);
      } catch (Throwable tt) {
        Logger.log(tt);
      }
    }
    Properties overrides = new Properties();
    try {
      overrides.load(BaseSystem.getBaseClassLoader().getResourceAsStream("settings_override.properties"));
      Logger.dump(overrides);
    } catch (Throwable t) {
      Logger.log(t);
      try {
        overrides.load(BaseSystem.class.getClassLoader().getResourceAsStream("settings_override.properties"));
        Logger.dump(overrides);
      } catch (Throwable tt) {
        Logger.log(tt);
      }
    }
    // add all overrides
    for (Object key : overrides.keySet()) {
      settings.put(key, overrides.get(key));
    }
    return settings;
  }

}
