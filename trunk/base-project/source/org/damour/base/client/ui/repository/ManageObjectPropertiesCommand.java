package org.damour.base.client.ui.repository;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;
import org.damour.base.client.ui.repository.properties.PropertiesPanel;
import org.damour.base.client.ui.repository.properties.PropertiesPanel.VIEW;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ManageObjectPropertiesCommand implements Command {

  PermissibleObject permissibleObject;

  public ManageObjectPropertiesCommand(PermissibleObject permissibleObject) {
    this.permissibleObject = permissibleObject;
  }

  public void execute() {
    try {
      if (permissibleObject != null) {
        final PromptDialogBox dialogBox = new PromptDialogBox("Properties", "OK", null, "Cancel", false, true);
        final AsyncCallback<Void> callback = new AsyncCallback<Void>() {
          public void onFailure(Throwable caught) {
            MessageDialogBox messageDialog = new MessageDialogBox("Error", caught.getMessage(), false, true, true);
            messageDialog.setCallback(new IDialogCallback() {
              public void okPressed() {
              }

              public void cancelPressed() {
                dialogBox.center();
              }
            });
            messageDialog.center();
          }

          public void onSuccess(Void result) {
          }
        };
        final PropertiesPanel propertiesPanel = new PropertiesPanel(permissibleObject, null, VIEW.GENERAL);
        dialogBox.setContent(propertiesPanel);
        dialogBox.setCallback(new IDialogCallback() {
          public void okPressed() {
            propertiesPanel.apply(callback);
          }

          public void cancelPressed() {
          }
        });
        dialogBox.center();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
