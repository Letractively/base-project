package org.damour.base.client.ui.repository;

import java.util.Date;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Photo;
import org.damour.base.client.ui.ToolTip;
import org.damour.base.client.utils.StringUtils;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.SourcesMouseEvents;

public class PermissibleObjectToolTip extends ToolTip {

  public PermissibleObjectToolTip(SourcesMouseEvents widget, PermissibleObject object, String thumbnailImageURL) {
    super();
    NumberFormat formatter = NumberFormat.getFormat("#,###");
    String tooltip = "";
    tooltip += "Name: " + object.getName();
    tooltip += "<BR>";
    tooltip += "Description: " + object.getDescription();
    if (object instanceof File) {
      tooltip += "<BR>";
      tooltip += "Type: " + ((File) object).getContentType();
    }
    tooltip += "<BR>";
    tooltip += "Date Created: " + (new Date(object.getCreationDate()).toLocaleString());
    tooltip += "<BR>";
    tooltip += "Last Modified: " + (new Date(object.getLastModifiedDate()).toLocaleString());
    tooltip += "<BR>";
    tooltip += "Owner: " + object.getOwner().getUsername();
    if (object instanceof File) {
      tooltip += "<BR>";
      tooltip += "Size: " + formatter.format(((File) object).getSize()) + " bytes";
    }

    if (StringUtils.isEmpty(thumbnailImageURL)) {
      if (object instanceof Photo) {
        Photo photo = (Photo) object;
        if (photo.getThumbnailImage() != null) {
          thumbnailImageURL = BaseApplication.getSettings().getString("GetFileService", BaseApplication.GET_FILE_SERVICE_PATH)
              + photo.getThumbnailImage().getId() + "_inline_" + photo.getName();
        }
      }
    }

    init(widget, thumbnailImageURL, tooltip);
  }

}
