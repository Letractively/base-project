package org.damour.base.client.ui.dialogs;

import org.damour.base.client.ui.IGenericCallback;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class MessageDialogBox extends PromptDialogBox {

  public MessageDialogBox(String title, String message, boolean isHTML, boolean autoHide, boolean modal) {
    super(title, "OK", null, null, autoHide, modal);
    setContent(isHTML ? new HTML(message) : new Label(message));
  }

  public static void alert(String message) {
    MessageDialogBox.alert("Alert", message, null);
  }
  
  public static void alert(String title, String message) {
    MessageDialogBox.alert(title, message, null);
  }
  
  public static void alert(String message, IGenericCallback<Void> callback) {
    MessageDialogBox.alert("Alert", message, callback);
  }

  public static void alert(final String title, final String message, final IGenericCallback<Void> callback) {
    PromptDialogBox pdb = new PromptDialogBox(title, "OK", null, null, false, true);
    pdb.setCallback(new IDialogCallback() {

      public void okPressed() {
        if (callback != null) {
          callback.invoke(null);
        }
      }

      public void cancelPressed() {
      }
    });
    pdb.setContent(new HTML(message));
    pdb.center();
  }

}
