package org.damour.base.client.objects;

import java.io.Serializable;

public class CategoryMembership implements Serializable, IHibernateFriendly {

  public Long id;
  public Category category;
  public PermissibleObject permissibleObject;

  public CategoryMembership() {
  }

  public String getCachePolicy() {
    return "nonstrict-read-write";
  }

  public boolean isLazy() {
    return false;
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

  public boolean isFieldMapped(String fieldName) {
    return true;
  }

  public String getFieldType(String fieldName) {
    return null;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public PermissibleObject getPermissibleObject() {
    return permissibleObject;
  }

  public void setPermissibleObject(PermissibleObject permissibleObject) {
    this.permissibleObject = permissibleObject;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

}