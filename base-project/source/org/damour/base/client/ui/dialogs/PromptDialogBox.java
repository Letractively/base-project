package org.damour.base.client.ui.dialogs;

import org.damour.base.client.ui.buttons.Button;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class PromptDialogBox extends DialogBox {

  IDialogCallback callback;
  IDialogValidatorCallback validatorCallback;
  Widget content;
  final FlexTable dialogContent = new FlexTable();

  public PromptDialogBox(String title, String okText, Button customButton, String cancelText, boolean autoHide, boolean modal) {
    super(autoHide, modal);
    setText(title);
    Button ok = new Button(okText);
    ok.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        if (validatorCallback == null || (validatorCallback != null && validatorCallback.validate())) {
          hide();
          if (callback != null) {
            callback.okPressed();
          }
        }
      }
    });
    final HorizontalPanel dialogButtonPanel = new HorizontalPanel();
    dialogButtonPanel.setSpacing(2);
    dialogButtonPanel.add(ok);
    if (customButton != null) {
      dialogButtonPanel.add(customButton);
    }
    if (cancelText != null) {
      Button cancel = new Button(cancelText);
      cancel.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          hide();
          if (callback != null) {
            callback.cancelPressed();
          }
        }
      });
      dialogButtonPanel.add(cancel);
    }
    HorizontalPanel dialogButtonPanelWrapper = new HorizontalPanel();
    if (okText != null && cancelText != null) {
      dialogButtonPanelWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    } else {
      dialogButtonPanelWrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    }
    dialogButtonPanelWrapper.setStyleName("dialogButtonPanel");
    dialogButtonPanelWrapper.setWidth("100%");
    dialogButtonPanelWrapper.add(dialogButtonPanel);

    dialogContent.setCellPadding(0);
    dialogContent.setCellSpacing(0);
    // add button panel
    dialogContent.setWidget(1, 0, dialogButtonPanelWrapper);
    dialogContent.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_BOTTOM);
    // dialogContent.getFlexCellFormatter().setColSpan(2, 0, 2);
    dialogContent.setWidth("100%");
    setWidget(dialogContent);
  }

  public boolean onKeyDownPreview(char key, int modifiers) {
    // Use the popup's key preview hooks to close the dialog when either
    // enter or escape is pressed.
    switch (key) {
    case KeyboardListener.KEY_ENTER:
      if (validatorCallback == null || (validatorCallback != null && validatorCallback.validate())) {
        hide();
        if (callback != null) {
          callback.okPressed();
        }
      }
      break;
    case KeyboardListener.KEY_ESCAPE:
      if (callback != null) {
        callback.cancelPressed();
      }
      hide();
      break;
    }
    return true;
  }

  public IDialogCallback getCallback() {
    return callback;
  }

  public void setContent(Widget content) {
    this.content = content;
    if (content != null) {

      VerticalPanel contentWrapper = new VerticalPanel();
      contentWrapper.setStyleName("dialogContentPanel");
      contentWrapper.add(content);
      contentWrapper.setWidth("100%");
      //content.getElement().setAttribute("margin", "5px");
      
      dialogContent.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
      dialogContent.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
      dialogContent.setWidget(0, 0, contentWrapper);
    }
    if (content instanceof FocusWidget) {
      setFocusWidget((FocusWidget) content);
    }
  }

  public Widget getContent() {
    return content;
  }

  public void setCallback(IDialogCallback callback) {
    this.callback = callback;
  }

  public IDialogValidatorCallback getValidatorCallback() {
    return validatorCallback;
  }

  public void setValidatorCallback(IDialogValidatorCallback validatorCallback) {
    this.validatorCallback = validatorCallback;
  }

}
