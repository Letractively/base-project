package org.damour.base.client.objects;

import java.io.Serializable;
import java.util.HashMap;

public class PermissibleObjectTreeNode implements Serializable {

  public PermissibleObject parent = null;
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

  public PermissibleObject getParent() {
    return parent;
  }

  public void setParent(PermissibleObject parent) {
    this.parent = parent;
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
