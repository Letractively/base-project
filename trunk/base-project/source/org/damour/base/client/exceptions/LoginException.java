package org.damour.base.client.exceptions;

public class LoginException extends RuntimeException {

  public LoginException() {
    super("Login failed");
  }

  public LoginException(String message) {
    super(message);
  }

}
