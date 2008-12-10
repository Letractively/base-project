package org.damour.base.client.ui.repository;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Photo;
import org.damour.base.client.soundmanager.MP3Player;
import org.damour.base.client.ui.dialogs.PromptDialogBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

public class OpenFileCommand implements Command {

  File object;
  boolean preview;

  public OpenFileCommand(File object, boolean preview) {
    this.object = object;
    this.preview = preview;
  }

  public void execute() {
    // if (download) {
    // url += "&download=true";
    // final Frame hidden = new Frame(url);
    // DOM.setStyleAttribute(hidden.getElement(), "display", "none");
    // RootPanel.get().add(hidden);
    // return;
    // }

    if (object instanceof File) {
      if (object instanceof Photo) {
        Photo photo = preview && ((Photo) object).getSlideshowImage() != null ? ((Photo) object).getSlideshowImage() : (Photo) object;
        String url = "/servlet/GetFileService?file=" + photo.getId() + "&name=" + photo.getName();
        if (!GWT.isScript()) {
          url = "http://localhost/files/" + photo.getNameOnDisk();
        }
        Image image = new Image(url);
        image.setHeight(photo.getHeight() + "px");
        image.setWidth(photo.getWidth() + "px");
        final PromptDialogBox promptDialog = new PromptDialogBox("Preview", "Close", null, null, true, true);
        promptDialog.setContent(image);
        promptDialog.center();
      } else if (object.getContentType().equals("audio/mpeg")) {
        String url = "/servlet/GetFileService?file=" + object.getId() + "&name=" + object.getName();
        if (!GWT.isScript()) {
          url = "http://localhost/files/" + object.getNameOnDisk();
        }
        String name = ((File) object).getName();
        MP3Player.getInstance().addSoundToPlayList(name, url);
        MP3Player.getInstance().play();
        MP3Player.getInstance().show();
      } else {
        String url = "/servlet/GetFileService?file=" + object.getId() + "&name=" + object.getName() + "&download=true";
        if (!GWT.isScript()) {
          url = "http://localhost/files/" + object.getNameOnDisk() + "?download=true";
        }
        final Frame hidden = new Frame(url);
        DOM.setStyleAttribute(hidden.getElement(), "display", "none");
        RootPanel.get().add(hidden);
      }
    }
  }

}
