package org.damour.base.client.ui.repository;

import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.service.BaseServiceAsync;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.IDialogValidatorCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextBox;

public class CreateNewFolderCommand implements Command {

  RepositoryTree repositoryTree;
  IRepositoryCallback repositoryCallback;

  public CreateNewFolderCommand(RepositoryTree repositoryTree, IRepositoryCallback repositoryCallback) {
    this.repositoryTree = repositoryTree;
    this.repositoryCallback = repositoryCallback;
  }

  public void execute() {
    final TextBox folderNameTextBox = new TextBox();
    folderNameTextBox.setVisibleLength(60);
    PromptDialogBox dialogBox = new PromptDialogBox("Enter New Folder Name", "OK", null, "Cancel", false, true);
    dialogBox.setContent(folderNameTextBox);
    dialogBox.setCallback(new IDialogCallback() {
      public void okPressed() {
        Folder parentFolder = null;
        if (repositoryTree.getLastItem() != null && repositoryTree.getLastItem().getUserObject() instanceof PermissibleObject) {
          PermissibleObject permissibleObject = (PermissibleObject) repositoryTree.getLastItem().getUserObject();
          parentFolder = permissibleObject.getParentFolder();
        }
        AsyncCallback<Folder> callback = new AsyncCallback<Folder>() {
          public void onFailure(Throwable caught) {
            MessageDialogBox messageDialog = new MessageDialogBox("Error", caught.getMessage(), false, true, true);
            messageDialog.center();
          }

          public void onSuccess(Folder newFolder) {
            repositoryTree.setLastItemId(newFolder.getId());
            repositoryTree.fetchRepositoryTree(repositoryCallback);
          }
        };
        Folder newFolder = new Folder();
        newFolder.setParentFolder(parentFolder);
        newFolder.setName(folderNameTextBox.getText());
        newFolder.setDescription(folderNameTextBox.getText());
        BaseServiceAsync.service.createNewFolder(newFolder, callback);
      }

      public void cancelPressed() {
      }
    });
    dialogBox.setValidatorCallback(new IDialogValidatorCallback() {
      public boolean validate() {
        return (folderNameTextBox.getText() != null && !"".equals(folderNameTextBox.getText()));
      }
    });
    dialogBox.center();

  }

}
