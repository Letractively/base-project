package org.damour.base.client.ui.buttons;

import java.util.ArrayList;
import java.util.List;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.utils.CursorUtils;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

public class ComboMenuButton extends HorizontalPanel {

  public static final String STYLE = "toolBarButton";

  private MenuBar menuBar;
  private Command command;
  private boolean enabled = true;
  private List<ClickHandler> clickHandlers = new ArrayList<ClickHandler>();
  private Image arrowImage = new Image();

  public ComboMenuButton(String labelText, MenuBar menuBar) {
    this.menuBar = menuBar;

    sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS);

    Label label = new Label(labelText, true);
    label.setStyleName("toolBarButtonLabel");
    // label.addMouseListener(this);
    add(label);
    // prevent double-click from selecting text
    CursorUtils.preventTextSelection(getElement());
    CursorUtils.preventTextSelection(label.getElement());

    arrowImage.setResource(BaseImageBundle.images.downArrow());
    // arrowImage.addMouseListener(this);
    arrowImage.setStyleName("toolBarButtonImage");
    add(arrowImage);

    // getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
    // getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);
    setStyleName(STYLE);
  }

  public ComboMenuButton(String labelText, MenuBar menuBar, Command command) {
    this(labelText, menuBar);
    this.command = command;
  }

  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    if ((event.getTypeInt() & Event.ONCLICK) == Event.ONCLICK) {
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
    } else if ((event.getTypeInt() & Event.ONMOUSEOVER) == Event.ONMOUSEOVER) {
      if (enabled) {
        addStyleDependentName("hover");
      }
    } else if ((event.getTypeInt() & Event.ONMOUSEOUT) == Event.ONMOUSEOUT) {
      if (enabled) {
        removeStyleDependentName("pressed");
        removeStyleDependentName("hover");
      }
    } else if ((event.getTypeInt() & Event.ONMOUSEUP) == Event.ONMOUSEUP) {
      if (enabled) {
        removeStyleDependentName("pressed");
        if (command != null) {
          try {
            command.execute();
          } catch (Exception e) {
            // don't fail because some idiot you are calling fails
          }
        }
        for (ClickHandler clickHandler : clickHandlers) {
          try {
            clickHandler.onClick(new com.google.gwt.event.dom.client.ClickEvent() {
              public Object getSource() {
                return this;
              }
            });
          } catch (Exception e) {
            // don't fail because some idiot you are calling fails
          }
        }
      }
    }
  }

  public void addClickListener(ClickHandler handler) {
    clickHandlers.add(handler);
  }

  public void removeClickListener(ClickHandler handler) {
    clickHandlers.remove(handler);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (enabled) {
      arrowImage.setResource(BaseImageBundle.images.downArrow());
      removeStyleDependentName("disabled");
    } else {
      arrowImage.setResource(BaseImageBundle.images.downArrowDisabled());
      addStyleDependentName("disabled");
    }
  }

}
