package org.damour.base.client.ui.dialogs;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class TimedMessageDialogBox extends DialogBox {

  public TimedMessageDialogBox(String title, String message, int showTime, boolean isHTML, boolean autoHide, boolean modal) {
    super(autoHide, modal);
    setText(title);
    Widget messageLabel = null;
    if (isHTML) {
      messageLabel = new HTML(message, true);
    } else {
      messageLabel = new Label(message, true);
    }
    messageLabel.setWidth("100%");
    FlexTable dialogContent = new FlexTable();
    dialogContent.setStyleName("dialogContentPanel");
    dialogContent.setWidth("400px");
    dialogContent.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
    dialogContent.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
    dialogContent.setWidget(0, 0, messageLabel);
    dialogContent.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
    dialogContent.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_LEFT);
    setWidget(dialogContent);
    Timer timer = new Timer() {
      public void run() {
        hide();
      }
    };
    timer.schedule(showTime);
  }
}
