package org.damour.base.client.exceptions;

public class SimpleMessageException extends RuntimeException {

  public SimpleMessageException() {
    super("Exception");
  }

  public SimpleMessageException(String message) {
    super("" + message);
  }

}
