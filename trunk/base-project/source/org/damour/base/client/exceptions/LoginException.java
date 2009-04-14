package org.damour.base.client.exceptions;

public class LoginException extends Exception {

  public LoginException() {
    super("Login failed");
  }

  public LoginException(String message) {
    super(message);
  }

}
