package org.damour.base.client.ui.admin;

import java.util.List;

import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;

public interface IAdminPanel {
  public void setUsers(List<User> users);
  public void setUserGroups(List<UserGroup> groups);
  public List<User> getUsers();
  public List<UserGroup> getUserGroups();
}
