package org.damour.base.client.ui.authentication;

import org.damour.base.client.objects.User;

public interface IAuthenticationListener {
  public void loggedOut();
  public void setAuthenticatedUser(User user);
}
