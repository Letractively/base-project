package org.damour.base.client.ui.buttons;

import java.util.ArrayList;
import java.util.List;

import org.damour.base.client.images.BaseImageBundle;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

public class ComboMenuButton extends FlexTable implements MouseListener {

  public static final String STYLE = "menuButton";

  private MenuBar menuBar;
  private Command command;
  private boolean enabled = true;
  private List<ClickListener> listeners = new ArrayList<ClickListener>();
  private Image arrowImage = new Image();

  
  public ComboMenuButton(String labelText, MenuBar menuBar) {
    this.menuBar = menuBar;

    setCellPadding(0);
    setCellSpacing(0);

    Label label = new Label(labelText, true);
    label.setStyleName("menuButtonLabel");
    label.addMouseListener(this);
    setWidget(0, 0, label);
    // prevent double-click from selecting text
    preventTextSelection(label.getElement());

    BaseImageBundle.images.downArrow().applyTo(arrowImage);
    arrowImage.addMouseListener(this);
    arrowImage.setStyleName("menuButtonImage");
    setWidget(0, 1, arrowImage);
    getCellFormatter().setStyleName(0, 1, "menuButtonImage");

    getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
    getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);
    setStyleName(STYLE);
  }

  public ComboMenuButton(String labelText, MenuBar menuBar, Command command) {
    this(labelText, menuBar);
    this.command = command;
  }

  public void onMouseDown(final Widget sender, final int x, final int y) {
    if (enabled) {
      addStyleDependentName("pressed");
      removeStyleDependentName("hover");
      final PopupPanel popup = MenuButtonCommand.popup;
      popup.setWidget(menuBar);
      popup.setPopupPositionAndShow(new PositionCallback() {
        public void setPosition(int offsetWidth, int offsetHeight) {
          popup.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight());
        }
      });
    }
  }

  public void onMouseEnter(Widget sender) {
    if (enabled) {
      addStyleDependentName("hover");
    }
  }

  public void onMouseLeave(Widget sender) {
    if (enabled) {
      removeStyleDependentName("pressed");
      removeStyleDependentName("hover");
    }
  }

  public void onMouseMove(Widget sender, int x, int y) {
  }

  public void onMouseUp(final Widget sender, final int x, final int y) {
    if (enabled) {
      removeStyleDependentName("pressed");
      if (command != null) {
        try {
          command.execute();
        } catch (Exception e) {
          // don't fail because some idiot you are calling fails
        }
      }
      for (ClickListener listener : listeners) {
        try {
          listener.onClick(this);
        } catch (Exception e) {
          // don't fail because some idiot you are calling fails
        }
      }
    }
  }

  public void addClickListener(ClickListener listener) {
    listeners.add(listener);
  }

  public void removeClickListener(ClickListener listener) {
    listeners.remove(listener);
  }

  private static native void preventTextSelection(Element ele) /*-{
         ele.onselectstart=function() {return false};
         ele.ondragstart=function() {return false};
       }-*/;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (enabled) {
      BaseImageBundle.images.downArrow().applyTo(arrowImage);
      removeStyleDependentName("disabled");
    } else {
      BaseImageBundle.images.downArrowDisabled().applyTo(arrowImage);
      addStyleDependentName("disabled");
    }
  }

}
