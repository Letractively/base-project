package org.damour.base.client.ui.dialogs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.RootPanel;

public class PopupPanel extends com.google.gwt.user.client.ui.PopupPanel implements CloseHandler<com.google.gwt.user.client.ui.PopupPanel> {

  private FocusPanel glassPanel = null;
  private int clickCount = 0;
  private FocusWidget focusWidget = null;
  boolean autoHide = false;
  boolean modal = true;
  boolean centerCalled = false;

  public PopupPanel(boolean autoHide, boolean modal) {
    super(autoHide, modal);
    this.autoHide = autoHide;
    this.modal = modal;
    addCloseHandler(this);
    Window.addResizeHandler(new ResizeHandler() {
      public void onResize(ResizeEvent event) {
        if (glassPanel != null) {
          glassPanel.setSize("100%", Window.getClientHeight() + Window.getScrollTop() + "px"); //$NON-NLS-1$
        }
      }
    });
  }

  public boolean onKeyDownPreview(char key, int modifiers) {
    // Use the popup's key preview hooks to close the dialog when either
    // enter or escape is pressed.
    switch (key) {
    case KeyCodes.KEY_ESCAPE:
      hide();
      break;
    }
    return true;
  }

  public void center() {
    // IE6 has problems with 100% height so is better a huge size
    // pageBackground.setSize("100%", "100%");
    if (glassPanel == null) {
      glassPanel = new FocusPanel();
      glassPanel.setStyleName("base-glass-panel"); //$NON-NLS-1$
      glassPanel.addClickHandler(new ClickHandler() {

        public void onClick(ClickEvent event) {
          clickCount++;
          if (clickCount > 2) {
            clickCount = 0;
            glassPanel.setVisible(false);
          }
        }
      });
      RootPanel.get().add(glassPanel, 0, 0);
    }
    super.center();
    if (modal && !centerCalled) {
      glassPanel.setSize("100%", Window.getClientHeight() + Window.getScrollTop() + "px"); //$NON-NLS-1$
      glassPanel.setVisible(true);
      centerCalled = true;
    }
    if (focusWidget != null) {
      focusWidget.setFocus(true);
    }
  }

  public void show() {
    super.show();
    if (focusWidget != null) {
      focusWidget.setFocus(true);
    }
  }

  public void setFocusWidget(FocusWidget widget) {
    focusWidget = widget;
    if (focusWidget != null) {
      focusWidget.setFocus(true);
    }
  }

  public void onClose(CloseEvent<com.google.gwt.user.client.ui.PopupPanel> event) {
    if (modal) {
      centerCalled = false;
      glassPanel.setVisible(false);
    }
  }
}