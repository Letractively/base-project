package org.damour.base.client.objects;

import java.io.Serializable;

public class Folder extends PermissibleObject implements Serializable, IHibernateFriendly {

  public boolean hidden = false;

  public Folder() {
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

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

}