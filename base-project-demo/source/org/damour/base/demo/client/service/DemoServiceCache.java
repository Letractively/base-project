package org.damour.base.demo.client.service;

import org.damour.base.client.BaseApplication;

import com.google.gwt.core.client.GWT;

public class DemoServiceCache {
  private static DemoServiceAsync service = (DemoServiceAsync) GWT.create(DemoService.class);

  public static DemoServiceAsync getServiceUnsafe() {
    return service;
  }

  public static DemoServiceAsync getService() {
    while (!BaseApplication.isInitialized()) {
      // sleep
    }
    return service;
  }
}