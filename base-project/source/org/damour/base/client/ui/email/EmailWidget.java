package org.damour.base.client.ui.email;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.PermissibleObject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class EmailWidget extends HorizontalPanel {

  private Image mailIcon = new Image();
  
  private PermissibleObject permissibleObject;
  private String message;
  private String subject;
  
  private ClickHandler clickHandler = new ClickHandler() {
    public void onClick(ClickEvent event) {
      EmailDialog emailDialog = new EmailDialog(permissibleObject, subject, message);
      emailDialog.center();
    }
  };

  public EmailWidget(PermissibleObject permissibleObject, String subject, String message, boolean showLabel, boolean labelOnLeft) {
    this.permissibleObject = permissibleObject;
    this.message = message;
    this.subject = subject;
    
    setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    BaseImageBundle.images.email16x16().applyTo(mailIcon);
    Label emailLabel = new Label("Email");
    if (showLabel && labelOnLeft) {
      DOM.setStyleAttribute(mailIcon.getElement(), "marginLeft", "3px");
    } else if (showLabel && !labelOnLeft) {
      DOM.setStyleAttribute(mailIcon.getElement(), "marginRight", "3px");
    }
    if (showLabel) {
      if (labelOnLeft) {
        add(emailLabel);
        add(mailIcon);
      } else {
        add(mailIcon);
        add(emailLabel);
      }
    } else {
      add(mailIcon);
    }
    mailIcon.addClickHandler(clickHandler);
    emailLabel.addClickHandler(clickHandler);
    mailIcon.setTitle("Email this!");
    emailLabel.setTitle("Email this!");
    DOM.setStyleAttribute(emailLabel.getElement(), "cursor", "hand");
    DOM.setStyleAttribute(emailLabel.getElement(), "cursor", "pointer");
    DOM.setStyleAttribute(mailIcon.getElement(), "cursor", "hand");
    DOM.setStyleAttribute(mailIcon.getElement(), "cursor", "pointer");
  }

  public PermissibleObject getPermissibleObject() {
    return permissibleObject;
  }

  public void setPermissibleObject(PermissibleObject permissibleObject) {
    this.permissibleObject = permissibleObject;
  }

}
