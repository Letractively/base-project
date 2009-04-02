package org.damour.base.server;

import javax.servlet.http.HttpServletRequest;

public class BaseSystem {

  private static String domainName = null;
  
  public static String getDomainName() {
    return domainName;
  }
  
  public static String getDomainName(HttpServletRequest request) {
    if (domainName == null) {
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
  
  
}
