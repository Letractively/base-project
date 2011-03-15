package org.damour.base.client.utils;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class CursorUtils {

  public static void setBusyCursor(Widget widget) {
    setBusyCursor(widget.getElement());
  }

  public static void setBusyCursor(Element element) {
    DOM.setStyleAttribute(element, "cursor", "wait");
  }

  public static void setDefaultCursor(Widget widget) {
    setDefaultCursor(widget.getElement());
  }

  public static void setDefaultCursor(Element element) {
    DOM.setStyleAttribute(element, "cursor", "default");
  }

  public static void setHandCursor(Widget widget) {
    setHandCursor(widget.getElement());
  }

  public static void setHandCursor(Element element) {
    DOM.setStyleAttribute(element, "cursor", "pointer");
    DOM.setStyleAttribute(element, "cursor", "hand");
  }

  public static native void preventTextSelection(Element ele)
  /*-{
    ele.onselectstart=function() {return false};
    ele.ondragstart=function() {return false};
    ele.onmousedown=function() {return false};
  }-*/;
  
}
