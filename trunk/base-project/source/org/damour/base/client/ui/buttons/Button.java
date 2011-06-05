package org.damour.base.client.ui.buttons;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;

public class Button extends com.google.gwt.user.client.ui.Button implements ClickHandler {

  private Command command;

  public Button() {
    super();
    init();
  }

  public Button(String text) {
    this();
    setText(text);
  }

  public Button(String text, final Command cmd) {
    this(text);
    setCommand(cmd);
  }

  public void init() {
    setStyleName("base-button");
  }

  public void setText(final String text) {
    setHTML(text);
  }

  public Command getCommand() {
    return command;
  }

  public void setCommand(final Command command) {
    this.command = command;
    addClickHandler(this);
  }

  public void onClick(ClickEvent event) {
    if (command != null) {
      command.execute();
    }
  }

}
