package org.damour.base.client.objects;

import java.io.Serializable;

public class Category implements Serializable, IHibernateFriendly {

  public Long id;
  public Category parentCategory;

  public Category() {
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

  public Category getParentCategory() {
    return parentCategory;
  }

  public void setParentCategory(Category parentCategory) {
    this.parentCategory = parentCategory;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

}