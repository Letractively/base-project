package org.damour.base.client.ui.repository;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Photo;
import org.damour.base.client.soundmanager.MP3Player;
import org.damour.base.client.ui.dialogs.PromptDialogBox;

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
        String url = BaseApplication.getSettings().getString("GetFileService", BaseApplication.GET_FILE_SERVICE_PATH) + photo.getId() + "_inline_" + photo.getName();
        Image image = new Image(url);
        image.setHeight(photo.getHeight() + "px");
        image.setWidth(photo.getWidth() + "px");
        final PromptDialogBox promptDialog = new PromptDialogBox("Preview", "Close", null, null, true, false);
        promptDialog.setContent(image);
        promptDialog.center();
      } else if ("audio/mpeg".equals(object.getContentType())) {
        String url = BaseApplication.getSettings().getString("GetFileService", BaseApplication.GET_FILE_SERVICE_PATH) + object.getId() + "_inline_" + object.getName();
        String name = ((File) object).getName();
        MP3Player.getInstance().addSoundToPlayList(name, url);
        MP3Player.getInstance().play();
        MP3Player.getInstance().show();
      } else {
        String url = BaseApplication.getSettings().getString("GetFileService", BaseApplication.GET_FILE_SERVICE_PATH) + object.getId() + "_attachment_" + object.getName();
        final Frame hidden = new Frame(url);
        DOM.setStyleAttribute(hidden.getElement(), "display", "none");
        RootPanel.get().add(hidden);
      }
    }
  }

}
