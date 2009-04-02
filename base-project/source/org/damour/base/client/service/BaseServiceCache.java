package org.damour.base.client.service;

import org.damour.base.client.BaseEntryPoint;

import com.google.gwt.core.client.GWT;

public class BaseServiceCache {
  private static BaseServiceAsync service = (BaseServiceAsync) GWT.create(BaseService.class);  

  public static BaseServiceAsync getServiceUnsafe() {
    return service;
  }
  
  public static BaseServiceAsync getService() {
    while (!BaseEntryPoint.isReady()) {
      // sleep
    }
    return service;
  }
}