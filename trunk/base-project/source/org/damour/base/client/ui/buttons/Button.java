package org.damour.base.client.ui.buttons;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

public class Button extends com.google.gwt.user.client.ui.Button {
  public Button() {
    super();
    init();
    sinkEvents(Event.MOUSEEVENTS);
  }

  public Button(String text) {
    super(text);
    init();
    sinkEvents(Event.MOUSEEVENTS);
    addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
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
      //setStyleName("my-button-hover");
    } else if ((event.getTypeInt() & Event.ONMOUSEOUT) == Event.ONMOUSEOUT) {
      setStyleName("my-button");
    }
  }
  
  public void init() {
    setStyleName("my-button");
  }
  
  public static native void blur(Element e)/*-{
  e.blur();
 }-*/;
}
