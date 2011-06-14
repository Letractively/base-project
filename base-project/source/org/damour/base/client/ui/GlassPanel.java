package org.damour.base.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class GlassPanel {

  private static GlassPanel instance = new GlassPanel();
 
  private FocusPanel focusPanel = new FocusPanel();
  private int clickCount = 0;
  
  
  private GlassPanel() {
    focusPanel.setStyleName("base-glass-panel");
    focusPanel.addStyleName("hasBusyCursor");
    focusPanel.addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent event) {
         if (instance.focusPanel.isVisible()) {
           clickCount++;
         }
         if (clickCount > 1) {
           setVisible(false);
         }
      }
    });
  }

  public static void setVisible(boolean visible) {
    if (visible) {
      if (!instance.focusPanel.isAttached()) {
        RootPanel.get().add(instance.focusPanel, 0, 0);
      }
      instance.focusPanel.setSize("100%", Window.getClientHeight() + Window.getScrollTop() + "px"); //$NON-NLS-1$
    } else {
      instance.clickCount = 0;
    }
    instance.focusPanel.setVisible(visible);
  }

}
