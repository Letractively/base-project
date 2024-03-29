package org.damour.base.client.ui.buttons;

import org.damour.base.client.utils.CursorUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;

public class IconButton extends FlexTable implements MouseListener {

  private String STYLE = "toolBarButton";

  private Command command;
  private boolean enabled = true;
  private Object userObject;

  private Label label = new Label();

  private Image image = new Image();
  private ImageResource defaultImage;
  private ImageResource hoverImage;
  private ImageResource disabledImage;
  private ImageResource pressedImage;

  public IconButton(String labelText, boolean labelOnLeft, ImageResource defaultImage, ImageResource hoverImage,
      ImageResource pressedImage, ImageResource disabledImage) {
    this.defaultImage = defaultImage;
    this.hoverImage = hoverImage;
    this.pressedImage = pressedImage;
    this.disabledImage = disabledImage;

    setCellPadding(0);
    setCellSpacing(0);

    addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        if (command != null) {
          try {
            command.execute();
          } catch (Exception e) {
            // don't fail because some idiot you are calling fails
          }
        }
      }
    });

    label.setWordWrap(false);
    label.setText(labelText);
    label.setStyleName(STYLE + "Label");
    label.addMouseListener(this);

    image.setUrl(defaultImage.getURL());
    image.setStyleName(STYLE + "Image");
    image.addMouseListener(this);

    if (labelText != null) {
      if (labelOnLeft) {
        setWidget(0, 0, label);
        setWidget(0, 1, image);
      } else {
        setWidget(0, 0, image);
        setWidget(0, 1, label);
      }
      // prevent double-click from selecting text
      CursorUtils.preventTextSelection(label.getElement());

      getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
      getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);
    } else {
      setWidget(0, 0, image);
      getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
    }

    setStyleName(STYLE);
  }

  public void onMouseDown(final Widget sender, final int x, final int y) {
    if (enabled) {
      addStyleDependentName("pressed");
      removeStyleDependentName("hover");
      image.setUrl(pressedImage.getURL());
    }
  }

  public void onMouseEnter(Widget sender) {
    if (enabled) {
      addStyleDependentName("hover");
      image.setUrl(hoverImage.getURL());
    }
  }

  public void onMouseLeave(Widget sender) {
    if (enabled) {
      removeStyleDependentName("pressed");
      removeStyleDependentName("hover");
      image.setUrl(defaultImage.getURL());
    }
  }

  public void onMouseMove(Widget sender, int x, int y) {
  }

  public void onMouseUp(final Widget sender, final int x, final int y) {
    if (enabled) {
      removeStyleDependentName("pressed");
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (enabled) {
      removeStyleDependentName("disabled");
      image.setUrl(defaultImage.getURL());
    } else {
      addStyleDependentName("disabled");
      image.setUrl(disabledImage.getURL());
    }
  }

  public Command getCommand() {
    return command;
  }

  public void setCommand(Command command) {
    this.command = command;
  }

  public void setText(String labelText) {
    label.setText(labelText);
  }

  public Object getUserObject() {
    return userObject;
  }

  public void setUserObject(Object userObject) {
    this.userObject = userObject;
  }

  public String getSTYLE() {
    return STYLE;
  }

  public void setSTYLE(String style) {
    STYLE = style;
    setStyleName(STYLE);
    label.setStyleName(STYLE + "Label");
    image.setStyleName(STYLE + "Image");
  }

}
