package org.damour.base.client.ui.repository;

import org.damour.base.client.objects.FileUploadStatus;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PopupPanel;
import org.damour.base.client.ui.progressbar.ProgressBar;
import org.damour.base.client.ui.progressbar.ProgressBar.TextFormatter;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;

public class FileUploadPanel extends FormPanel {
  private int STATUS_UPDATE_INTERVAL = 1000;
  private PopupPanel progressPopup = new PopupPanel(false, true);
  private ProgressBar progressMeter = new ProgressBar(0, 5);
  // Create a FileUpload widget.
  private FileUpload upload = new FileUpload();

  FileUploadStatus result;

  private Timer uploadStatusTimer = new Timer() {
    public void run() {
      AsyncCallback<FileUploadStatus> callback = new AsyncCallback<FileUploadStatus>() {
        public void onFailure(Throwable caught) {
        }

        public void onSuccess(FileUploadStatus result) {
          FileUploadPanel.this.result = result;
          progressMeter.setProgress(result.getStatus());
          progressMeter.setTextVisible(true);
        }
      };
      BaseServiceCache.getService().getFileUploadStatus(callback);
    }
  };

  public FileUploadPanel(final IFileUploadCallback callback, PermissibleObject parentFolder, String formActionUrl) {
    // objectType = an instance of the content object to be published
    // on the server, this guy just needs to be someone who has getData/setData
    // and get/set mimetypes

    progressMeter.setMinProgress(0);
    progressMeter.setTextFormatter(new TextFormatter() {
      protected String getText(ProgressBar bar, double curProgress) {
        String percentText = (int) (100 * bar.getPercent()) + "%";
        if (result.getStatus() == FileUploadStatus.UPLOADING) {
          return "Uploading... " + percentText;
        } else if (result.getStatus() == FileUploadStatus.WRITING_DATABASE) {
          return "Writing to Database... " + percentText;
        }
        return "Saving... " + percentText;
      }
    });

    if (parentFolder != null) {
      formActionUrl += "?parentFolder=" + parentFolder.getId();
    }

    setAction(formActionUrl);
    // Because we're going to add a FileUpload widget, we'll need to set the
    // form to use the POST method, and multipart MIME encoding.
    setEncoding(FormPanel.ENCODING_MULTIPART);
    setMethod(FormPanel.METHOD_POST);

    upload.getElement().setAttribute("size", "50");
    upload.setName("userfile");

    addSubmitHandler(new SubmitHandler() {
      public void onSubmit(SubmitEvent event) {
        Cookies.removeCookie(upload.getName());
        // This event is fired just before the form is submitted. We can take
        // this opportunity to perform validation.
        if (upload.getFilename().length() == 0) {
          MessageDialogBox dialog = new MessageDialogBox("Info", "The filename must not be empty", false, true, true);
          dialog.center();
          event.cancel();
        } else {
          progressMeter.setWidth("300px");
          progressPopup.setWidget(progressMeter);
          progressPopup.center();
          uploadStatusTimer.scheduleRepeating(STATUS_UPDATE_INTERVAL);
        }
      }
    });

    addSubmitCompleteHandler(new SubmitCompleteHandler() {
      public void onSubmitComplete(SubmitCompleteEvent event) {
        progressPopup.hide();
        uploadStatusTimer.cancel();
        String id = Cookies.getCookie(upload.getName());
        if (callback != null && id != null) {
          callback.fileUploaded(id);
        } else if (callback != null) {
          callback.uploadFailed();
        }
      }
    });

    DOM.setStyleAttribute(getElement(), "margin", "0px");
    setWidth("100%");
    setWidget(upload);
  }

  public void submit() {
    try {
      super.submit();
    } catch (Throwable t) {
      progressPopup.hide();
      uploadStatusTimer.cancel();
      MessageDialogBox messageDialog = new MessageDialogBox("Error", "Upload failed, check file permissions or filename.", false, true, true);
      messageDialog.center();
    }
  }

  public String getFilename() {
    return upload.getFilename();
  }

}
