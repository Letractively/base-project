package org.damour.base.client.objects;

import java.io.Serializable;

public class File extends PermissibleObject implements Serializable, IHibernateFriendly {

  public String contentType;

  public String nameOnDisk;

  // stored here so we can lazily load fileData
  public long size = -1;

  public File() {
  }

  public boolean isFieldUnique(String fieldName) {
    return false;
  }

  public boolean isFieldKey(String fieldName) {
    return false;
  }

  public String getSqlUpdate() {
    return null;
  }

  /**
   * @return the contentType
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * @param contentType
   *          the contentType to set
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * @return the size
   */
  public long getSize() {
    return size;
  }

  /**
   * @param size
   *          the size to set
   */
  public void setSize(long size) {
    this.size = size;
  }

  public String getNameOnDisk() {
    return nameOnDisk;
  }

  public void setNameOnDisk(String nameOnDisk) {
    this.nameOnDisk = nameOnDisk;
  }

}