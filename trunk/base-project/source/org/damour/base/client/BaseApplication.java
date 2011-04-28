package org.damour.base.client;

import java.util.ArrayList;
import java.util.List;

import org.damour.base.client.localization.IResourceBundleLoadCallback;
import org.damour.base.client.localization.ResourceBundle;
import org.damour.base.client.objects.Referral;
import org.damour.base.client.objects.User;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.IGenericCallback;
import org.damour.base.client.ui.authentication.AuthenticationHandler;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.utils.StringUtils;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;

public class BaseApplication implements EntryPoint, StartupListener {

  public static final String BASE_SERVICE_PATH = "/servlet/org.damour.base.server.BaseService";
  public static final String CAPTCHA_SERVICE_PATH = "/servlet/org.damour.base.server.CaptchaImageGeneratorService";
  public static final String FILE_UPLOAD_SERVICE_PATH = "/servlet/org.damour.base.server.FileUploadService";
  public static final String GET_FILE_SERVICE_PATH = "/files/";

  private static boolean loading = false;
  private static boolean initialized = false;
  private static List<StartupListener> startupListeners = new ArrayList<StartupListener>();

  private static ResourceBundle settings = null;
  private static ResourceBundle messages = null;

  public static native void setApplicationInitialized()
  /*-{
    $wnd.applicationInitialized = true;
  }-*/;

  public static native boolean isApplicationInitialized()
  /*-{
    if ($wnd.applicationInitialized == undefined) {
      return false;
    }
    return $wnd.applicationInitialized;
  }-*/;

  /**
   * The use of this method determines if this module should be loaded. The reason this was introduced was to allow modules which extend/embed this module to
   * prevent the script from loading altogether.
   * 
   * @return whether or not to load the module
   */
  public boolean attemptToLoadModule() {
    return !isApplicationInitialized();
  }

  public void onModuleLoad() {

    if (!attemptToLoadModule()) {
      return;
    }

    setApplicationInitialized();

    addStartupListener(this);
    // set default base service path
    ((ServiceDefTarget) BaseServiceCache.getServiceUnsafe()).setServiceEntryPoint(BASE_SERVICE_PATH);
    
    GWT.runAsync(new RunAsyncCallback() {
      
      public void onSuccess() {
        Referral referral = new Referral();
        referral.referralURL = Document.get().getReferrer();
        referral.url = Window.Location.getHref();
        BaseServiceCache.getServiceUnsafe().submitReferral(referral, new AsyncCallback<Void>() {
          
          public void onSuccess(Void result) {
          }
          
          public void onFailure(Throwable caught) {
          }
        });
      }
      
      public void onFailure(Throwable reason) {
      }
    });
    
    if (!loading) {
      loading = true;

      // this is an account validation mode
      if (!StringUtils.isEmpty(Window.Location.getParameter("u")) && !StringUtils.isEmpty(Window.Location.getParameter("v"))) {
        addStartupListener(new StartupListener() {
          public void loadModule() {
            String username = Window.Location.getParameter("u");
            String validationCode = Window.Location.getParameter("v");
            BaseServiceCache.getService().submitAccountValidation(username, validationCode, new AsyncCallback<User>() {
              public void onFailure(Throwable caught) {
                MessageDialogBox.alert(caught.getMessage());
              }

              public void onSuccess(User user) {
                if (user != null && user.isValidated()) {
                  AuthenticationHandler.getInstance().setUser(user);
                  AuthenticationHandler.getInstance().handleUserAuthentication(false);
                  MessageDialogBox.alert("Account validation successful.", new IGenericCallback<Void>() {
                    public void invoke(Void object) {
                      Window.Location.assign(Window.Location.getHref().substring(0, Window.Location.getHref().indexOf("?")));
                    }
                  });

                } else {
                  MessageDialogBox.alert("Could not validate account.");
                }
              }
            });
          }
        });
      }
      ResourceBundle.clearCache();
      // load settings, then messages
      loadSettings(new IGenericCallback<Void>() {
        public void invoke(Void object) {
          // now load messages
          loadMessages();
        }
      });
    }
  }

  public static boolean isInitialized() {
    return initialized;
  }

  public void loadSettings(final IGenericCallback<Void> callback) {
    settings = new ResourceBundle();
    settings.loadBundle("settings/", "settings", false, new IResourceBundleLoadCallback() {
      public void bundleLoaded(String bundleName) {
        final ResourceBundle settings_override = new ResourceBundle();
        settings_override.loadBundle("settings/", "settings_override", false, new IResourceBundleLoadCallback() {
          public void bundleLoaded(String bundleName) {
            settings.mergeResourceBundle(settings_override);
            String serviceEntryPoint = settings.getString("BaseService", BASE_SERVICE_PATH);
            if (!StringUtils.isEmpty(serviceEntryPoint)) {
              ((ServiceDefTarget) BaseServiceCache.getServiceUnsafe()).setServiceEntryPoint(serviceEntryPoint);
            }
            callback.invoke(null);
          }
        });
      }
    });
  }

  public void loadMessages() {
    // when the bundle is loaded, it will fire an event
    // calling our bundleLoaded
    messages = new ResourceBundle();
    messages.loadBundle("messages", "messages", true, new IResourceBundleLoadCallback() {
      public void bundleLoaded(String bundleName) {
        final ResourceBundle messages_override = new ResourceBundle();
        messages_override.loadBundle("messages", "messages_override", true, new IResourceBundleLoadCallback() {
          public void bundleLoaded(String bundleName) {
            messages.mergeResourceBundle(messages_override);
            clearLoadingIndicator();
            initialized = true;
            fireStartupListeners();
          }
        });
      }
    });
  }

  private static void addStartupListener(StartupListener listener) {
    if (isInitialized()) {
      listener.loadModule();
    } else {
      startupListeners.add(listener);
    }
  }

  private static void fireStartupListeners() {
    for (StartupListener startupListener : startupListeners) {
      startupListener.loadModule();
    }
  }

  private static void clearLoadingIndicator() {
    RootPanel loadingPanel = RootPanel.get("loading");
    if (loadingPanel != null) {
      loadingPanel.removeFromParent();
      loadingPanel.setVisible(false);
      loadingPanel.setHeight("0px");
    }
  }

  public static ResourceBundle getSettings() {
    return settings;
  }

  public static ResourceBundle getMessages() {
    return messages;
  }

  // override this
  public void loadModule() {
  }

}
