package org.damour.agelizer.client.service;

import org.damour.base.client.service.Utility;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class AgelizerServiceCache {

  public static AgelizerServiceAsync service = null;

  public static AgelizerServiceAsync getService() {
    Utility.setBusyCursor();
    if (service == null) {
      service = (AgelizerServiceAsync) GWT.create(AgelizerService.class);
      ServiceDefTarget endpoint = (ServiceDefTarget) service;
      endpoint.setServiceEntryPoint("/servlet/AgelizerServiceImpl");
    }
    return service;
  }
}
