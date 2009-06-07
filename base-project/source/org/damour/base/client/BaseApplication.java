package org.damour.base.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.damour.base.client.localization.IResourceBundleLoadCallback;
import org.damour.base.client.localization.ResourceBundle;
import org.damour.base.client.objects.User;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.IGenericCallback;
import org.damour.base.client.ui.authentication.AuthenticationHandler;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.utils.StringTokenizer;
import org.damour.base.client.utils.StringUtils;

import com.google.gwt.core.client.EntryPoint;
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

  private static Map<String, String> supportedLanguages = new HashMap<String, String>();

  private static ResourceBundle settings;
  private static ResourceBundle settings_override;
  private static ResourceBundle messages = null;
  private static ResourceBundle messages_override = null;

  public void onModuleLoad() {
    addStartupListener(this);
    // set default base service path
    ((ServiceDefTarget) BaseServiceCache.getServiceUnsafe()).setServiceEntryPoint(BASE_SERVICE_PATH);
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
                  MessageDialogBox.alert("Account validation successful.");
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
        settings_override = new ResourceBundle();
        settings_override.loadBundle("settings/", "settings_override", false, new IResourceBundleLoadCallback() {
          public void bundleLoaded(String bundleName) {
            settings.mergeResourceBundle(settings_override);
            setSupportedLanguages(settings.getString("supportedLanguages"));
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
    messages.setSupportedLocales(supportedLanguages);
    messages.loadBundle("messages", "messages", true, new IResourceBundleLoadCallback() {
      public void bundleLoaded(String bundleName) {
        messages_override = new ResourceBundle();
        messages_override.setSupportedLocales(supportedLanguages);
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

  private static void setSupportedLanguages(String langStr) {
    StringTokenizer langs = new StringTokenizer(langStr, ",");
    for (int i = 0; i < langs.countTokens(); i++) {
      StringTokenizer langToken = new StringTokenizer(langs.tokenAt(i), "=");
      String langCode = langToken.tokenAt(0).trim();
      String langDisplay = langToken.tokenAt(1).trim();
      supportedLanguages.put(langCode, langDisplay);
    }
  }

  public static Map<String, String> getSupportedLanguages() {
    return supportedLanguages;
  }

  // override this
  public void loadModule() {
  }

}
