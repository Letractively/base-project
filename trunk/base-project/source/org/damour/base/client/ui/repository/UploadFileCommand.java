package org.damour.base.client.ui.repository;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.ui.dialogs.IDialogValidatorCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;

import com.google.gwt.user.client.Command;

public class UploadFileCommand implements Command {

  IRepositoryCallback repositoryCallback;
  PermissibleObject permissibleObject;
  
  public UploadFileCommand(PermissibleObject permissibleObject, IRepositoryCallback repositoryCallback) {
    this.repositoryCallback = repositoryCallback;
    this.permissibleObject = permissibleObject;
  }

  public void execute() {
    final PromptDialogBox uploadDialog = new PromptDialogBox("Upload File", "Send", null, "Cancel", false, true);
    IFileUploadCallback fileUploadCallback = new IFileUploadCallback() {
      public void uploadFailed() {
        uploadDialog.hide();
        MessageDialogBox messageDialog = new MessageDialogBox("Error", "Upload failed, check file permissions.", false, true, true);
        messageDialog.center();
      }
      public void fileUploaded(String id) {
        uploadDialog.hide();
        if (id == null || "".equals(id)) {
          MessageDialogBox messageDialog = new MessageDialogBox("Error", "Upload failed, check file permissions.", false, true, true);
          messageDialog.center();
          return;
        }
        repositoryCallback.fileUploaded(id);
      }
    };
    PermissibleObject parentFolder = null;
    if (permissibleObject instanceof File) {
      parentFolder = permissibleObject.getParent();
    } else if (permissibleObject instanceof Folder) {
      parentFolder = (Folder) permissibleObject;
    }
    final FileUploadPanel fileuploader = new FileUploadPanel(fileUploadCallback, parentFolder, BaseApplication.getSettings().getString("FileUploadService", BaseApplication.FILE_UPLOAD_SERVICE_PATH));
    uploadDialog.setContent(fileuploader);
    uploadDialog.setValidatorCallback(new IDialogValidatorCallback() {
      public boolean validate() {
        fileuploader.submit();
        return false;
      }
    });
    uploadDialog.center();
  }

}
