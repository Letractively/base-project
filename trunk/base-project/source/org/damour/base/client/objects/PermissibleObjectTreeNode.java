package org.damour.base.client.objects;

import java.io.Serializable;
import java.util.HashMap;

public class PermissibleObjectTreeNode implements Serializable {

  public PermissibleObject object = null;
  public UserRating userRating = null;
  public UserAdvisory userAdvisory = null;
  public UserThumb userThumb = null;

  public HashMap<PermissibleObject, PermissibleObjectTreeNode> children = new HashMap<PermissibleObject, PermissibleObjectTreeNode>();

  public PermissibleObjectTreeNode() {
  }

  public HashMap<PermissibleObject, PermissibleObjectTreeNode> getChildren() {
    return children;
  }

  public void setChildren(HashMap<PermissibleObject, PermissibleObjectTreeNode> children) {
    this.children = children;
  }

  public PermissibleObject getObject() {
    return object;
  }

  public void setObject(PermissibleObject object) {
    this.object = object;
  }

  public UserRating getUserRating() {
    return userRating;
  }

  public void setUserRating(UserRating userRating) {
    this.userRating = userRating;
  }

  public UserAdvisory getUserAdvisory() {
    return userAdvisory;
  }

  public void setUserAdvisory(UserAdvisory userAdvisory) {
    this.userAdvisory = userAdvisory;
  }

  public UserThumb getUserThumb() {
    return userThumb;
  }

  public void setUserThumb(UserThumb userThumb) {
    this.userThumb = userThumb;
  }
}
