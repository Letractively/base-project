package org.damour.base.client.ui.authentication;

import com.google.gwt.user.client.Command;

public class CreateNewAccountCommand implements Command {

  public CreateNewAccountCommand() {
  }

  public void execute() {
    // if we are already logged in and are the administrator, then create a new AuthenticationHandler
    // so that we do not logout the admin and login the newly created user (that the admin created)
    if (AuthenticationHandler.getInstance().getUser() == null) {
      AuthenticationHandler.getInstance().showNewAccountDialog(false);
    } else {
      AuthenticationHandler.getNewInstance().showNewAccountDialog(false);
    }
  }
  
}
