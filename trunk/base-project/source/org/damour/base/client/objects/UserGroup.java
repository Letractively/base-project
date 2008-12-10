package org.damour.base.client.objects;

import java.io.Serializable;

public class UserGroup extends SecurityPrincipal implements Serializable, IHibernateFriendly {

  public String name;
  public String description;
  public User owner;
  public boolean autoJoin = false;
  public boolean visible = true;
  public boolean locked = false;

  public UserGroup() {
  }

  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((owner == null) ? 0 : owner.hashCode());
    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    UserGroup other = (UserGroup) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (owner == null) {
      if (other.owner != null)
        return false;
    } else if (!owner.equals(other.owner))
      return false;
    return true;
  }

  public boolean isFieldUnique(String fieldName) {
    return false;
  }

  public boolean isFieldKey(String fieldName) {
    return false;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isAutoJoin() {
    return autoJoin;
  }

  public void setAutoJoin(boolean autoJoin) {
    this.autoJoin = autoJoin;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isLocked() {
    return locked;
  }

  public void setLocked(boolean locked) {
    this.locked = locked;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

}