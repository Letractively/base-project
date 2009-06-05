package org.damour.base.client.ui.buttons;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;

public class ToolbarButton extends FlexTable implements MouseListener {

  public static final String STYLE = "toolBarButton";

  private Label label = new Label();
  private Command command;
  private boolean enabled = true;
  private List<ClickListener> listeners = new ArrayList<ClickListener>();

  public ToolbarButton(String labelText) {
    setCellPadding(0);
    setCellSpacing(0);

    label.setText(labelText);
    label.setWordWrap(false);
    label.setStyleName("toolBarButtonLabel");
    label.addMouseListener(this);
    setWidget(0, 0, label);
    // prevent double-click from selecting text
    preventTextSelection(label.getElement());

    getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
    setStyleName(STYLE);
  }

  public ToolbarButton(String labelText, Command command) {
    this(labelText);
    this.command = command;
  }

  public void onMouseDown(final Widget sender, final int x, final int y) {
    if (enabled) {
      addStyleDependentName("pressed");
      removeStyleDependentName("hover");
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

  private static native void preventTextSelection(Element ele)
  /*-{
    ele.onselectstart=function() {return false};
    ele.ondragstart=function() {return false};
  }-*/;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (enabled) {
      removeStyleDependentName("disabled");
    } else {
      addStyleDependentName("disabled");
    }
  }

  public void setText(String text) {
    label.setText(text);
  }

  public String getText() {
    return label.getText();
  }

  public Command getCommand() {
    return command;
  }

  public void setCommand(Command command) {
    this.command = command;
  }

}
