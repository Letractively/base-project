package org.damour.base.demo.client;

import org.damour.base.client.BaseApplicationUI;
import org.damour.base.client.ui.toolbar.ToolBar;

public class DemoApplication extends BaseApplicationUI {

  public void loadApplication() {
  }

  public ToolBar buildApplicationToolBar() {
    ToolBar tb = super.buildApplicationToolBar();
//    tb.add(new Label("hello"));
//    tb.addPadding(5);
    return tb;
  }

}
