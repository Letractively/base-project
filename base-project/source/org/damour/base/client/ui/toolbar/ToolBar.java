package org.damour.base.client.ui.toolbar;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class ToolBar extends HorizontalPanel {

  public ToolBar() {
    setHeight("29px");
    setStyleName("toolBar");
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

  public void addFiller(int percent) {
    HTML filler = new HTML("");
    super.add(filler);
    setCellWidth(filler, percent + "%");
  }

}
