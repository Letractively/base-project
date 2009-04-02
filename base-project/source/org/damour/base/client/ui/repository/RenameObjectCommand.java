package org.damour.base.client.ui.repository;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.IDialogValidatorCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextBox;

public class RenameObjectCommand implements Command {

  PermissibleObject permissibleObject;
  IRepositoryCallback repositoryCallback;

  public RenameObjectCommand(PermissibleObject permissibleObject, IRepositoryCallback repositoryCallback) {
    this.permissibleObject = permissibleObject;
    this.repositoryCallback = repositoryCallback;
  }

  public void execute() {
    final TextBox nameTextBox = new TextBox();
    nameTextBox.setVisibleLength(60);
    nameTextBox.setText(permissibleObject.getName());
    PromptDialogBox dialogBox = new PromptDialogBox("Enter New Name", "OK", null, "Cancel", false, true);
    dialogBox.setContent(nameTextBox);
    dialogBox.setCallback(new IDialogCallback() {
      public void okPressed() {
        final AsyncCallback<Void> renameCallback = new AsyncCallback<Void>() {
          public void onFailure(Throwable caught) {
            MessageDialogBox messageDialog = new MessageDialogBox("Error", caught.getMessage(), false, true, true);
            messageDialog.center();
          }

          public void onSuccess(Void nothing) {
            repositoryCallback.objectRenamed(permissibleObject);
          }
        };

        if (permissibleObject instanceof File) {
          File file = (File) permissibleObject;
          file.setName(nameTextBox.getText());
          BaseServiceCache.getService().renameFile(file, renameCallback);
        } else if (permissibleObject instanceof Folder) {
          Folder folder = (Folder) permissibleObject;
          folder.setName(nameTextBox.getText());
          BaseServiceCache.getService().renameFolder(folder, renameCallback);
        }
      }

      public void cancelPressed() {
      }
    });
    dialogBox.setValidatorCallback(new IDialogValidatorCallback() {
      public boolean validate() {
        return (nameTextBox.getText() != null && !"".equals(nameTextBox.getText()));
      }
    });
    dialogBox.center();
  }

}
