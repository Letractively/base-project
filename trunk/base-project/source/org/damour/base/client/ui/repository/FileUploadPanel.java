package org.damour.base.client.ui.repository;

import org.damour.base.client.objects.FileUploadStatus;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PopupPanel;
import org.damour.base.client.ui.progressbar.ProgressBar;
import org.damour.base.client.ui.progressbar.ProgressBar.TextFormatter;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FileUploadPanel extends FlexTable {
  private int STATUS_UPDATE_INTERVAL = 2500;
  private FormPanel form = new FormPanel();
  private PopupPanel progressPopup = new PopupPanel(false, true);
  private ProgressBar progressMeter = new ProgressBar();
  FileUploadStatus result;

  private Timer uploadStatusTimer = new Timer() {
    public void run() {
      AsyncCallback<FileUploadStatus> callback = new AsyncCallback<FileUploadStatus>() {
        public void onFailure(Throwable caught) {
        }

        public void onSuccess(FileUploadStatus result) {
          FileUploadPanel.this.result = result;
          progressMeter.setMaxProgress(result.getContentLength());
          if (result.getStatus() > FileUploadStatus.UPLOADING) {
            progressMeter.setProgress(result.getBytesWritten());
          } else {
            progressMeter.setProgress(result.getBytesRead());
          }
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
        } else if (result.getStatus() == FileUploadStatus.BUILDING_THUMBNAILS) {
          return "Building Thumbnails... " + percentText;
        }
        return "Saving... " + percentText;
      }
    });

    if (parentFolder != null) {
      formActionUrl += "?parentFolder=" + parentFolder.getId();
    }

    form.setAction(formActionUrl);
    // Because we're going to add a FileUpload widget, we'll need to set the
    // form to use the POST method, and multipart MIME encoding.
    form.setEncoding(FormPanel.ENCODING_MULTIPART);
    form.setMethod(FormPanel.METHOD_POST);

    final VerticalPanel uploadPanel = new VerticalPanel();

    // Create a FileUpload widget.
    final FileUpload upload = new FileUpload();
    upload.setName("userfile");

    form.addFormHandler(new FormHandler() {

      public void onSubmitComplete(FormSubmitCompleteEvent event) {
        progressPopup.hide();
        uploadStatusTimer.cancel();
        String id = Cookies.getCookie(upload.getName());
        if (callback != null) {
          callback.fileUploaded(id);
        }
      }

      public void onSubmit(FormSubmitEvent event) {
        // This event is fired just before the form is submitted. We can take
        // this opportunity to perform validation.
        if (upload.getFilename().length() == 0) {
          MessageDialogBox dialog = new MessageDialogBox("Info", "The filename must not be empty", false, true, true);
          dialog.center();
          event.setCancelled(true);
        } else {
          progressMeter.setWidth("300px");
          progressPopup.setWidget(progressMeter);
          progressPopup.center();
          uploadStatusTimer.scheduleRepeating(STATUS_UPDATE_INTERVAL);
        }
      }
    });

    uploadPanel.add(upload);
    form.setWidget(uploadPanel);

    setWidget(0, 0, form);
  }

  public void submit() {
    form.submit();
  }
}
