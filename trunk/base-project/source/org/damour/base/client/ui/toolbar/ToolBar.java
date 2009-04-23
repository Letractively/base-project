package org.damour.base.client.ui.toolbar;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class ToolBar extends HorizontalPanel {

  public ToolBar() {
    setHeight("29px");
    setStyleName("linkBar");
    setWidth("100%");
    setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
  }

  public void addPadding(int padding) {
    HTML padder = new HTML("");
    padder.setWidth(padding + "px");
    super.add(padder);
    setCellWidth(padder, padding + "px");
  }

  public void addFiller() {
    HTML filler = new HTML("");
    filler.setWidth("100%");
    super.add(filler);
    setCellWidth(filler, "100%");
  }

}
