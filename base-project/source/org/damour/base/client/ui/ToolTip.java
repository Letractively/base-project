package org.damour.base.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SourcesMouseEvents;
import com.google.gwt.user.client.ui.Widget;

public class ToolTip extends PopupPanel implements MouseListener {

  Timer hideTimer = new Timer() {
    public void run() {
      hide();
    }
  };

  Timer timer = new Timer() {
    public void run() {
      show();
    }
  };

  public ToolTip() {
    super(true, false);
  }
  
  public ToolTip(SourcesMouseEvents widget, String previewImageURL, String html) {
    super(true, false);
    init(widget, previewImageURL, html);
  }

  protected void init(SourcesMouseEvents widget, String previewImageURL, String html) {

    widget.addMouseListener(this);

    HorizontalPanel hp = new HorizontalPanel();
    hp.setSpacing(5);
    hp.setStyleName("tooltipText");
    if (previewImageURL != null) {
      Image previewImage = new Image(previewImageURL);
      hp.add(previewImage);
      DOM.setStyleAttribute(previewImage.getElement(), "border", "1px solid #c8c8c8");
    }
    hp.add(new HTML(html));

    setWidget(hp);
    setStyleName("tooltip");
  }

  public void onMouseDown(Widget sender, int x, int y) {
    hideTimer.cancel();
    timer.cancel();
    hide();
  }

  public void onMouseEnter(Widget sender) {
  }

  public void onMouseLeave(Widget sender) {
    hideTimer.cancel();
    timer.cancel();
    hide();
  }

  public void onMouseMove(final Widget sender, final int x, final int y) {
    if (!isAttached()) {
      timer.cancel();
      hideTimer.cancel();
      setPopupPosition(sender.getAbsoluteLeft() + x, sender.getAbsoluteTop() + sender.getOffsetHeight() + 20);
      timer.schedule(500);
      hideTimer.schedule(5500);
    }
  }

  public void onMouseUp(Widget sender, int x, int y) {
    hideTimer.cancel();
    timer.cancel();
    hide();
  }

}
