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

public class AdvertiseDialog extends PromptDialogBox implements IDialogValidatorCallback, IDialogCallback {

  private static TextBox contactName = new TextBox();
  private static TextBox email = new TextBox();
  private static TextBox company = new TextBox();
  private static TextBox phone = new TextBox();
  private static TextArea comments = new TextArea();

  public AdvertiseDialog() {
    super("Advertising", "Submit", null, "Cancel", false, true);

    contactName.setVisibleLength(50);
    email.setVisibleLength(50);
    company.setVisibleLength(50);
    phone.setVisibleLength(20);
    phone.setTextAlignment(TextBox.ALIGN_RIGHT);
    comments.setVisibleLines(4);
    comments.setWidth("100%");

    VerticalPanel vp = new VerticalPanel();
    vp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    Label advertLabel = new Label(BaseApplication.getMessages().getString("advertiseWithUs", "Advertise With Us!"));
    DOM.setStyleAttribute(advertLabel.getElement(), "fontWeight", "bold");
    vp.add(advertLabel);
    HTML advertiseDescription = new HTML(
        BaseApplication
            .getMessages()
            .getString(
                "advertiseDescription",
                "<BR>If you are interested in advertising on this website please fill out the form below. We will be happy to work with you regarding cost, ad placement and frequency.<BR><BR>"));
    vp.add(advertiseDescription);

    FlexTable formTable = new FlexTable();
    formTable.setText(0, 0, "Contact Name");
    formTable.setText(1, 0, "E-Mail");
    formTable.setText(2, 0, "Company");
    formTable.setText(3, 0, "Phone Number");
    formTable.setText(4, 0, "Comments");

    formTable.setWidget(0, 1, contactName);
    formTable.setWidget(1, 1, email);
    formTable.setWidget(2, 1, company);
    formTable.setWidget(3, 1, phone);
    formTable.setWidget(4, 1, comments);

    formTable.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
    formTable.getCellFormatter().setAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
    formTable.getCellFormatter().setAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
    formTable.getCellFormatter().setAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
    formTable.getCellFormatter().setAlignment(4, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);

    formTable.getCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
    formTable.getCellFormatter().setAlignment(1, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
    formTable.getCellFormatter().setAlignment(2, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
    formTable.getCellFormatter().setAlignment(3, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
    formTable.getCellFormatter().setAlignment(4, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);

    vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    vp.add(formTable);

    vp.setSize("500px", "240px");

    setContent(vp);

    setValidatorCallback(this);
    setCallback(this);
  }

  public boolean validate() {
    String errorStr = "";
    if (StringUtils.isEmpty(contactName.getText())) {
      errorStr += "Contact Name is missing.<BR>";
    }
    if (StringUtils.isEmpty(email.getText())) {
      errorStr += "E-Mail address is missing.<BR>";
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
      BaseServiceCache.getService().submitAdvertisingInfo(contactName.getText(), email.getText(), company.getText(), phone.getText(), comments.getText(),
          callback);
    }
    return false;
  }

  public void cancelPressed() {
    hide();
  }

  public void okPressed() {
  }

}
