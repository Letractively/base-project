package org.damour.base.client.objects;

import java.io.Serializable;
import java.util.HashMap;

public class PermissibleObjectTreeNode implements Serializable {

  public PermissibleObject parent = null;
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

}
