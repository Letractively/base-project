package org.damour.base.client;

import java.util.ArrayList;
import java.util.List;

import org.damour.base.client.localization.IResourceBundleLoadCallback;
import org.damour.base.client.localization.ResourceBundle;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.IGenericCallback;
import org.damour.base.client.utils.StringUtils;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class BaseEntryPoint implements EntryPoint, IResourceBundleLoadCallback {

  private static ResourceBundle settings;
  private static ResourceBundle messages;
  private static boolean ready = false;
  private static String serviceEntryPointDefault = "/servlet/org.damour.base.server.BaseService";
  private static List<IGenericCallback<Void>> startupListeners = new ArrayList<IGenericCallback<Void>>();

  public void onModuleLoad() {
    settings = new ResourceBundle("base_settings/", "base_settings", false, new IResourceBundleLoadCallback() {
      public void bundleLoaded(String bundleName) {
        messages = new ResourceBundle("base_messages/", "base_messages", true, BaseEntryPoint.this);
      }
    });
    ((ServiceDefTarget) BaseServiceCache.getServiceUnsafe()).setServiceEntryPoint(serviceEntryPointDefault);
  }

  public void bundleLoaded(String bundleName) {
    String serviceEntryPoint = settings.getString("BaseServiceServiceEntryPoint", serviceEntryPointDefault);
    if (!StringUtils.isEmpty(serviceEntryPoint)) {
      ((ServiceDefTarget) BaseServiceCache.getServiceUnsafe()).setServiceEntryPoint(serviceEntryPoint);
    }
    ready = true;
    for (IGenericCallback<Void> listener : startupListeners) {
      listener.invokeGenericCallback(null);
    }
    startupListeners.clear();
  }

  public static boolean isReady() {
    return ready;
  }

  public static void addBaseStartupListener(IGenericCallback<Void> listener) {
    if (isReady()) {
      listener.invokeGenericCallback(null);
    } else {
      startupListeners.add(listener);
    }
  }

  public static ResourceBundle getSettings() {
    return settings;
  }

  public static ResourceBundle getMessages() {
    return messages;
  }

}
