package org.damour.base.client.ui.dialogs;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class MessageDialogBox extends PromptDialogBox {

  public MessageDialogBox(String title, String message, boolean isHTML, boolean autoHide, boolean modal) {
    super(title, "OK", null, null, autoHide, modal);
    setContent(isHTML ? new HTML(message) : new Label(message));
  }

  public static void alert(String message) {
    MessageDialogBox.alert("Alert", message);
  }

  public static void alert(String title, String message) {
    PromptDialogBox pdb = new PromptDialogBox(title, "OK", null, null, false, true);
    pdb.setContent(new HTML(message));
    pdb.center();
  }

}
