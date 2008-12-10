package org.damour.base.client.service;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class BaseServiceStartup implements EntryPoint {

  public void onModuleLoad() {
    ((ServiceDefTarget)BaseServiceAsync.service).setServiceEntryPoint("/servlet/BaseService");
  }

}
