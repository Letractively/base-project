package org.damour.base.client.ui.buttons;

import org.damour.base.client.utils.CursorUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class Button extends HorizontalPanel {

  private Command command;
  private Label label = new Label();
  
  public Button() {
    super();
    init();
    sinkEvents(Event.MOUSEEVENTS);
    label.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        setStyleName("my-button");
        if (command != null) {
          command.execute();
        }
      }
    });
    add(label);
    CursorUtils.preventTextSelection(getElement());
    CursorUtils.preventTextSelection(label.getElement());
  }

  public Button(String text) {
    this();
    label.setText(text);
  }

  public Button(String text, Command cmd) {
    this(text);
    setCommand(cmd);
  }

  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    if ((event.getTypeInt() & Event.ONMOUSEOVER) == Event.ONMOUSEOVER) {
      setStyleName("my-button-hover");
    } else if ((event.getTypeInt() & Event.ONMOUSEDOWN) == Event.ONMOUSEDOWN) {
      setStyleName("my-button-pressed");
    } else if ((event.getTypeInt() & Event.ONMOUSEUP) == Event.ONMOUSEUP) {
      setStyleName("my-button-hover");
      blur(getElement());
    } else if ((event.getTypeInt() & Event.ONMOUSEMOVE) == Event.ONMOUSEMOVE) {
      // too expensive
      // setStyleName("my-button-hover");
    } else if ((event.getTypeInt() & Event.ONMOUSEOUT) == Event.ONMOUSEOUT) {
      setStyleName("my-button");
    }
  }

  public void init() {
    setStyleName("my-button");
  }

  public void setText(final String text) {
    label.setText(text);
  }
  
  public static native void blur(Element e)
  /*-{
    e.blur();
  }-*/;

  /**
   * Gets whether this widget is enabled.
   * 
   * @return <code>true</code> if the widget is enabled
   */
  public boolean isEnabled() {
    return !DOM.getElementPropertyBoolean(getElement(), "disabled");
  }

  /**
   * Sets whether this widget is enabled.
   * 
   * @param enabled
   *          <code>true</code> to enable the widget, <code>false</code> to disable it
   */
  public void setEnabled(boolean enabled) {
    DOM.setElementPropertyBoolean(getElement(), "disabled", !enabled);
  }

  public Command getCommand() {
    return command;
  }

  public void setCommand(Command command) {
    this.command = command;
  }

  public void addClickHandler(ClickHandler handler) {
    label.addClickHandler(handler);
  }

  public void addClickListener(ClickListener listener) {
    label.addClickListener(listener);
  }
  
}
