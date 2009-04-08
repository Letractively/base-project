package org.damour.base.client.service;

import org.damour.base.client.BaseApplication;

import com.google.gwt.core.client.GWT;

public class BaseServiceCache {
  private static BaseServiceAsync service = (BaseServiceAsync) GWT.create(BaseService.class);

  public static BaseServiceAsync getServiceUnsafe() {
    return service;
  }

  public static BaseServiceAsync getService() {
    while (!BaseApplication.isInitialized()) {
      // sleep
    }
    return service;
  }
}