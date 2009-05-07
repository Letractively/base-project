package org.damour.base.client.ui.repository;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Photo;

public class UrlBuilder {

  public static String getImageUrl(Photo photo) {
    String url = BaseApplication.getSettings().getString("GetFileService", BaseApplication.GET_FILE_SERVICE_PATH) + photo.getId() + "_inline_"
        + photo.getName();
    return url;
  }

  public static String getImageThumbnailUrl(Photo photo) {
    String url = BaseApplication.getSettings().getString("GetFileService", BaseApplication.GET_FILE_SERVICE_PATH) + photo.getThumbnailImage().getId()
        + "_inline_" + photo.getThumbnailImage().getName();
    return url;
  }

  public static String getImageSlideshowUrl(Photo photo) {
    String url = BaseApplication.getSettings().getString("GetFileService", BaseApplication.GET_FILE_SERVICE_PATH) + photo.getSlideshowImage().getId()
        + "_inline_" + photo.getSlideshowImage().getName();
    return url;
  }

  public static String getImagePreviewUrl(Photo photo) {
    String url = BaseApplication.getSettings().getString("GetFileService", BaseApplication.GET_FILE_SERVICE_PATH) + photo.getPreviewImage().getId()
        + "_inline_" + photo.getPreviewImage().getName();
    return url;
  }

  public static String getUrl(PermissibleObject object) {
    String url = BaseApplication.getSettings().getString("GetFileService", BaseApplication.GET_FILE_SERVICE_PATH) + object.getId() + "_inline_"
        + object.getName();
    return url;
  }

}
