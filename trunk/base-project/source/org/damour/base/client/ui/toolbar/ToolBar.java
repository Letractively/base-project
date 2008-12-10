package org.damour.base.client.ui.toolbar;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ToolBar extends HorizontalPanel {

  public ToolBar() {
    setHeight("29px");
    setStyleName("linkBar");
    setWidth("100%");
    setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
  }
  
  public void add(Widget w) {
    if (getWidgetCount() == 0) {
      DOM.setStyleAttribute(w.getElement(), "paddingLeft", "10px");
      DOM.setStyleAttribute(w.getElement(), "paddingRight", "5px");
    }
    super.add(w);
  }
  
}
