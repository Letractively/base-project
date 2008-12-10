package org.damour.base.client.ui.admin;

import java.util.List;

import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;

public interface IAdminCallback {
  public void userGroupsFetched(List<UserGroup> groups);
  public void usersFetched(List<User> users);
  public void updateUser(User user);
  public void updateUserGroup(UserGroup group);
}
