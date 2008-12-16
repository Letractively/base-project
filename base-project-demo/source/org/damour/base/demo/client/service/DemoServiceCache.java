package org.damour.base.demo.client.service;

import org.damour.base.client.service.Utility;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class DemoServiceCache {

  public static DemoServiceAsync service = null;

  public static DemoServiceAsync getService() {
    Utility.setBusyCursor();
    if (service == null) {
      service = (DemoServiceAsync) GWT.create(DemoService.class);
      ServiceDefTarget endpoint = (ServiceDefTarget) service;
      endpoint.setServiceEntryPoint("/servlet/DemoServiceImpl");
    }
    return service;
  }
}
