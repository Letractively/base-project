package org.damour.base.client.objects;

import java.io.Serializable;

public class Tag implements Serializable, IHibernateFriendly {

  public Long id;
  public String name;
  public String description;
  public Tag parentTag;

  public Tag() {
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

  public String getFieldType(String fieldName) {
    return null;
  }

  public String getSqlUpdate() {
    return null;
  }

  public boolean isFieldMapped(String fieldName) {
    return true;
  }

  public Tag getParentTag() {
    return parentTag;
  }

  public void setParentTag(Tag parentTag) {
    this.parentTag = parentTag;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}