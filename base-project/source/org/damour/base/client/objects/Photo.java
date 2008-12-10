package org.damour.base.client.objects;

import java.io.Serializable;

public class Photo extends File implements Serializable, IHibernateFriendly {

  public PhotoThumbnail slideshowImage;
  public PhotoThumbnail thumbnailImage;
  public int height;
  public int width;

  public Photo() {
  }

  public PhotoThumbnail getSlideshowImage() {
    return slideshowImage;
  }

  public void setSlideshowImage(PhotoThumbnail slideshowImage) {
    this.slideshowImage = slideshowImage;
  }

  public PhotoThumbnail getThumbnailImage() {
    return thumbnailImage;
  }

  public void setThumbnailImage(PhotoThumbnail thumbnailImage) {
    this.thumbnailImage = thumbnailImage;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

}
