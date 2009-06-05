package org.damour.base.client.ui.buttons;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

public class Button extends com.google.gwt.user.client.ui.Label {
  public Button() {
    super();
    init();
    sinkEvents(Event.MOUSEEVENTS);
  }

  public Button(String text) {
    super(text);
    init();
    sinkEvents(Event.MOUSEEVENTS);
    addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        setStyleName("my-button");
      }
    });
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

}
