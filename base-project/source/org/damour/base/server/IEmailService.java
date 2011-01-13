package org.damour.base.server;

public interface IEmailService {
  public void sendDebugMessage(String text);
  public boolean sendMessage(String smtpHost, String fromAddress, String fromName, String to, String subject, String text);
  public void emailException(Throwable t);
}