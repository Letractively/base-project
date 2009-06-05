package org.damour.base.client.ui.dialogs;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.ui.IGenericCallback;
import org.damour.base.client.utils.StringUtils;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;

public class PrivacyPolicyDialog extends PromptDialogBox {

  private static String policyString;

  public PrivacyPolicyDialog() {
    super("Privacy Policy", "OK", null, null, true, false);
  }

  public void center() {
    IGenericCallback<String> callback = new IGenericCallback<String>() {
      public void invoke(String object) {
        HTML text = new HTML(policyString);
        ScrollPanel scroller = new ScrollPanel(text);
        setContent(scroller);
        scroller.setWidth("600px");
        scroller.setHeight("400px");
        PrivacyPolicyDialog.super.center();
      }
    };

    if (StringUtils.isEmpty(policyString)) {
      fetchPrivacyString(callback);
    } else {
      callback.invoke(policyString);
      super.center();
    }
  }

  private void fetchPrivacyString(final IGenericCallback<String> callback) {
    // populate with text from the policy
    RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, "messages/privacy.html");
    try {
      rb.setCallback(new RequestCallback() {
        public void onError(Request request, Throwable exception) {
          MessageDialogBox.alert("Error", exception.getMessage());
        }

        public void onResponseReceived(Request request, Response response) {
          policyString = response.getText().replaceAll("\\{companyName\\}", BaseApplication.getMessages().getString("companyName", "Base-Project"));
          if (callback != null) {
            callback.invoke(policyString);
          }
        }
      });
      rb.send();
    } catch (RequestException e) {
      MessageDialogBox.alert("Error", e.getMessage());
    }
  }

}
