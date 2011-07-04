package org.damour.base.client.objects;

public class Tag extends PermissibleObject {

  public Tag parentTag;

  public Tag() {
  }

  public Tag getParentTag() {
    return parentTag;
  }

  public void setParentTag(Tag parentTag) {
    this.parentTag = parentTag;
  }

}