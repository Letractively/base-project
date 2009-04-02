package org.damour.base.client.ui.repository;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

public class DeleteObjectCommand implements Command {

  PermissibleObject permissibleObject;
  IRepositoryCallback repositoryCallback;

  public DeleteObjectCommand(PermissibleObject permissibleObject, IRepositoryCallback repositoryCallback) {
    this.permissibleObject = permissibleObject;
    this.repositoryCallback = repositoryCallback;
  }

  public void execute() {
    PromptDialogBox dialogBox = new PromptDialogBox("Question", "Yes", null, "No", false, true);
    dialogBox.setContent(new Label("Delete " + permissibleObject.getName() + "?"));
    dialogBox.setCallback(new IDialogCallback() {
      public void okPressed() {
        final AsyncCallback<Void> deleteCallback = new AsyncCallback<Void>() {
          public void onFailure(Throwable caught) {
            MessageDialogBox messageDialog = new MessageDialogBox("Error", caught.getMessage(), false, true, true);
            messageDialog.center();
          }

          public void onSuccess(Void nothing) {
            repositoryCallback.fileDeleted();
          }
        };

        if (permissibleObject != null && permissibleObject instanceof File) {
          BaseServiceCache.getService().deleteFile((File) permissibleObject, deleteCallback);
        } else if (permissibleObject != null && permissibleObject instanceof Folder) {
          BaseServiceCache.getService().deleteFolder((Folder) permissibleObject, deleteCallback);
        }
      }

      public void cancelPressed() {
      }
    });
    dialogBox.center();
  }

}
