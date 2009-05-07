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

  private FocusPanel pageBackground = null;
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
        if (pageBackground != null) {
          pageBackground.setSize("100%", Window.getClientHeight() + Window.getScrollTop() + "px"); //$NON-NLS-1$
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
    if (pageBackground == null) {
      pageBackground = new FocusPanel();
      pageBackground.setStyleName("modalDialogPageBackground"); //$NON-NLS-1$
      pageBackground.addClickHandler(new ClickHandler() {

        public void onClick(ClickEvent event) {
          clickCount++;
          if (clickCount > 2) {
            clickCount = 0;
            pageBackground.setVisible(false);
          }
        }
      });
      RootPanel.get().add(pageBackground, 0, 0);
    }
    super.center();
    if (modal && !centerCalled) {
      pageBackground.setSize("100%", Window.getClientHeight() + Window.getScrollTop() + "px"); //$NON-NLS-1$
      pageBackground.setVisible(true);
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
      pageBackground.setVisible(false);
    }
  }
}