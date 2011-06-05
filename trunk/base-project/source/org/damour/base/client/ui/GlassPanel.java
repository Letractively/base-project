package org.damour.base.client.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class GlassPanel {

  private static FocusPanel instance = new FocusPanel();

  private GlassPanel() {
    instance.setStyleName("base-glass-panel");
  }

  public static void setVisible(boolean visible) {
    if (visible) {
      if (!instance.isAttached()) {
        RootPanel.get().add(instance, 0, 0);
      }
      instance.setSize("100%", Window.getClientHeight() + Window.getScrollTop() + "px"); //$NON-NLS-1$
    }
    instance.setVisible(visible);
  }

}
