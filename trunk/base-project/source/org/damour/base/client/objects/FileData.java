package org.damour.base.client.objects;

import java.io.Serializable;

public class FileData implements Serializable, IHibernateFriendly {

  public Long id;

  public PermissibleObject permissibleObject;

  // this field is put in hibernate, but it is not serialized over the wire, that would be bad
  // some databases have a limit to how much you can send in a packet
  public transient byte[] data;

  public FileData() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public PermissibleObject getPermissibleObject() {
    return permissibleObject;
  }

  public void setPermissibleObject(PermissibleObject permissibleObject) {
    this.permissibleObject = permissibleObject;
  }

  public String getCachePolicy() {
    return "none";
  }

  public String getSqlUpdate() {
    return null;
  }

  public boolean isFieldKey(String fieldName) {
    return false;
  }

  public boolean isFieldUnique(String fieldName) {
    return false;
  }

  public boolean isLazy() {
    return false;
  }

  public boolean isFieldMapped(String fieldName) {
    return true;
  }

}
