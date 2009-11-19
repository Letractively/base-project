package org.damour.base.client.ui.dialogs;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.utils.StringUtils;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FeedbackDialog extends PromptDialogBox implements IDialogValidatorCallback, IDialogCallback {

  private static TextBox contactName = new TextBox();
  private static TextBox email = new TextBox();
  private static TextBox phone = new TextBox();
  private static TextArea comments = new TextArea();

  public FeedbackDialog() {
    super(BaseApplication.getMessages().getString("feedback", "Feedback"), "Submit", null, "Cancel", false, true);
    super.setAllowKeyboardEvents(false);
    
    contactName.setVisibleLength(50);
    email.setVisibleLength(50);
    phone.setVisibleLength(20);
    phone.setTextAlignment(TextBox.ALIGN_RIGHT);
    comments.setVisibleLines(4);
    comments.setWidth("100%");

    VerticalPanel vp = new VerticalPanel();
    vp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    Label feedbackLabel = new Label(BaseApplication.getMessages().getString("feedbackTitle", "Give Us Feedback"));
    DOM.setStyleAttribute(feedbackLabel.getElement(), "fontWeight", "bold");
    vp.add(feedbackLabel);
    HTML feedbackDescription = new HTML(
        BaseApplication
            .getMessages()
            .getString(
                "feedbackDescription",
                "<BR>If you are reporting a bug, please include as much detail as you can so that our engineers can work to resolve the problem as quickly as possible.<BR><BR>If you are submitting a feature request or enhancement, we will attempt to work with you if we accept your idea.  You may keep your description brief.<BR><BR>For other comments and criticisms, simply fill out as much of the form as you want.  <BR><BR>"));
    vp.add(feedbackDescription);

    FlexTable formTable = new FlexTable();
    formTable.setText(0, 0, "Contact Name");
    formTable.setText(1, 0, "E-Mail");
    formTable.setText(2, 0, "Phone Number");
    formTable.setText(3, 0, "Comments or Description");

    formTable.setWidget(0, 1, contactName);
    formTable.setWidget(1, 1, email);
    formTable.setWidget(2, 1, phone);
    formTable.setWidget(3, 1, comments);

    formTable.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
    formTable.getCellFormatter().setAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
    formTable.getCellFormatter().setAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
    formTable.getCellFormatter().setAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);

    formTable.getCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
    formTable.getCellFormatter().setAlignment(1, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
    formTable.getCellFormatter().setAlignment(2, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
    formTable.getCellFormatter().setAlignment(3, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);

    vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    vp.add(formTable);

    vp.setSize("500px", "240px");

    setContent(vp);

    setValidatorCallback(this);
    setCallback(this);
  }

  public boolean validate() {
    String errorStr = "";
    if (StringUtils.isEmpty(email.getText())) {
      errorStr += "E-Mail address is missing.<BR>";
    }
    if (StringUtils.isEmpty(comments.getText())) {
      errorStr += "Comments are missing.<BR>";
    }
    if (!StringUtils.isEmpty(errorStr)) {
      MessageDialogBox.alert("Error", errorStr);
    } else {
      // perform the submission
      AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
        public void onFailure(Throwable caught) {
          MessageDialogBox.alert("Error", caught.getMessage());
        }

        public void onSuccess(Boolean result) {
          if (result) {
            hide();
            MessageDialogBox.alert("Info", "Your submission has been sent.  Thank you.");
          } else {
            MessageDialogBox.alert("Error", "There was an error submitting your feedback.  Please try again later.");
          }
        }
      };
      BaseServiceCache.getService().submitFeedback(contactName.getText(), email.getText(), phone.getText(), comments.getText(), callback);
    }
    return false;
  }

  public void cancelPressed() {
    hide();
  }

  public void okPressed() {
  }

}
