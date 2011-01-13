package org.damour.base.server;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailHelper implements IEmailService {

  private static EmailHelper instance = new EmailHelper();

  public EmailHelper() {
    System.out.println("EmailHelper instanced");
  }
  
  public static EmailHelper getInstance() {
    return instance;
  }

  public void sendDebugMessage(String text) {
    String from = "admin@" + BaseSystem.getDomainName();
    String to = from;
    String subject = BaseSystem.getDomainName() + " DEBUG";
    String message = "<BR/>" + text + "<BR/>";
    sendMessage(BaseSystem.getSmtpHost(), from, from, to, subject, message);
  }

  public boolean sendMessage(String smtpHost, String fromAddress, String fromName, String to, String subject, String text) {
    try {
      // Get system properties
      Properties props = System.getProperties();
      // Setup mail server
      props.put("mail.smtp.host", smtpHost);
      // Get session
      javax.mail.Session session = javax.mail.Session.getDefaultInstance(props, null);
      // Define message
      MimeMessage message = new MimeMessage(session);
      // Set the from address
      message.setFrom(new InternetAddress(fromAddress, fromName));
      // Set the to address
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      // Set the subject
      message.setSubject(subject);
      // Set the content
      message.setContent(text, "text/html");
      // Send message
      Transport.send(message);
      return true;
    } catch (Exception e) {
      Logger.log(e);
      return false;
    }
  }

  public void emailException(Throwable t) {
    String trace = Logger.convertStringToHTML(Logger.convertThrowableToString(t));
    String from = "admin@" + BaseSystem.getDomainName();
    String to = from;
    String subject = "A critical server error has occurred.";
    String message = "<BR/>" + t.getMessage() + "<BR/>" + trace;
    sendMessage(BaseSystem.getSmtpHost(), from, from, to, subject, message);
  }

}
