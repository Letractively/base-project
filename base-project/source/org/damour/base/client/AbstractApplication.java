package org.damour.base.client;

import org.damour.base.client.localization.IResourceBundleLoadCallback;
import org.damour.base.client.localization.ResourceBundle;
import org.damour.base.client.ui.IGenericCallback;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public abstract class AbstractApplication implements EntryPoint {

  private ResourceBundle messages = null;
  private ResourceBundle messages_override = null;

  public void onModuleLoad() {
    BaseEntryPoint.addBaseStartupListener(new IGenericCallback<Void>() {
      public void invokeGenericCallback(Void object) {
        // the base has been loaded, now we can safely load
        // this is due to asynchronous calls
        // when the bundle is loaded, it will fire an event
        // calling our bundleLoaded
        messages = new ResourceBundle("messages", "messages", true, new IResourceBundleLoadCallback() {
          public void bundleLoaded(String bundleName) {
            messages_override = new ResourceBundle("messages", "messages_override", true, new IResourceBundleLoadCallback() {
              public void bundleLoaded(String bundleName) {
                messages.mergeResourceBundle(messages_override);
                clearLoadingIndicator();
                loadModule();
              }
            });
          }
        });
      }
    });
  }

  private void clearLoadingIndicator() {
    RootPanel loadingPanel = RootPanel.get("loading");
    if (loadingPanel != null) {
      loadingPanel.removeFromParent();
      loadingPanel.setVisible(false);
      loadingPanel.setHeight("0px");
    }
  }

  public abstract void loadModule();

  public ResourceBundle getMessages() {
    return messages;
  }

}
