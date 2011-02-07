package org.damour.base.client.ui.email;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.User;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.authentication.AuthenticationHandler;
import org.damour.base.client.ui.buttons.Button;
import org.damour.base.client.ui.dialogs.DialogBox;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.scrolltable.ScrollTable;
import org.damour.base.client.utils.StringUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class EmailDialog extends DialogBox {

  private static String[] columnWidths = new String[] { "370px", "100px" };
  private static ScrollTable emailAddressTable;
  private static final int NUM_ADDRESSES = 5;

  private static TextBox userTextBox = new TextBox();
  private static TextBox userEmailTextBox = new TextBox();

  private PermissibleObject permissibleObject;

  public EmailDialog(final PermissibleObject permissibleObject, final String subject, final String message) {
    super(false, true);

    this.permissibleObject = permissibleObject;

    final User user = AuthenticationHandler.getInstance().getUser();

    setText("E-mail to Friends");
    Button ok = new Button("OK");
    ok.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        // build a map of addresses/names from table
        String toAddresses = "";
        for (int i = 0; i < NUM_ADDRESSES; i++) {
          String address = ((TextBox) emailAddressTable.getWidget(i, 0)).getText();
          if (!StringUtils.isEmpty(address)) {
            String name = ((TextBox) emailAddressTable.getWidget(i, 1)).getText();
            if (StringUtils.isEmpty(name)) {
              name = address;
            }
            toAddresses += address + ";" + name + ";";
          }
        }
        String fromAddress = null;
        String fromName = null;
        if (user == null) {
          fromAddress = userEmailTextBox.getText();
          fromName = userTextBox.getText();
        } else {
          fromAddress = user.getEmail();
          fromName = user.getFirstname();
        }
        if (StringUtils.isEmpty(fromName)) {
          fromName = fromAddress;
        }
        if (StringUtils.isEmpty(fromAddress)) {
          MessageDialogBox messageDialog = new MessageDialogBox("Error", "You must enter your email address.", false, false, true);
          messageDialog.center();
          return;
        }
        sendEmail(permissibleObject, subject, message, fromAddress, fromName, toAddresses);
        hide();
      }
    });
    Button cancel = new Button("Cancel");
    cancel.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        hide();
      }
    });
    final HorizontalPanel dialogButtonPanel = new HorizontalPanel();
    dialogButtonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    dialogButtonPanel.add(ok);
    dialogButtonPanel.add(cancel);
    FlexTable dialogContent = new FlexTable();
    // dialogContent.setStyleName("dialogContentPanel");
    dialogContent.setWidth("480px");
    dialogContent.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
    dialogContent.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);

    if (user == null) {
      FlexTable userInfoTable = new FlexTable();
      userInfoTable.setWidget(0, 0, new Label("Enter your name and email below."));
      userInfoTable.getFlexCellFormatter().setColSpan(0, 0, 2);
      userInfoTable.setWidget(1, 0, new Label("Name:"));
      setFocusWidget(userTextBox);
      userInfoTable.setWidget(1, 1, userTextBox);
      userInfoTable.getFlexCellFormatter().setWidth(1, 1, "100%");
      userInfoTable.setWidget(2, 0, new Label("Email:"));
      userEmailTextBox.setWidth("200px");
      userInfoTable.setWidget(2, 1, userEmailTextBox);
      userInfoTable.getFlexCellFormatter().setWidth(2, 1, "100%");
      dialogContent.setWidget(0, 0, userInfoTable);
    }

    dialogContent.setWidget(1, 0, new Label("E-Mail this to up " + NUM_ADDRESSES + " friends.  Just enter their e-mail addresses in the form below.", true));

    if (emailAddressTable == null) {
      emailAddressTable = new ScrollTable(columnWidths, false);
      emailAddressTable.setHeaderWidget(0, new Label("E-Mail Address"), HasHorizontalAlignment.ALIGN_LEFT);
      emailAddressTable.setHeaderWidget(1, new Label("Name"), HasHorizontalAlignment.ALIGN_LEFT);

      TextBox firstAddressTextBox = null;
      for (int i = 0; i < NUM_ADDRESSES; i++) {
        TextBox addressTextBox = new TextBox();
        if (i == 0) {
          firstAddressTextBox = addressTextBox;
        }
        DOM.setStyleAttribute(addressTextBox.getElement(), "border", "0px");
        addressTextBox.setWidth("100%");
        emailAddressTable.setDataWidget(i, 0, addressTextBox, HasHorizontalAlignment.ALIGN_LEFT);
        TextBox nameTextBox = new TextBox();
        DOM.setStyleAttribute(nameTextBox.getElement(), "border", "0px");
        nameTextBox.setWidth("100%");
        emailAddressTable.setDataWidget(i, 1, nameTextBox, HasHorizontalAlignment.ALIGN_LEFT);
      }
      if (user != null) {
        setFocusWidget(firstAddressTextBox);
      }
    }

    dialogContent.setWidget(2, 0, emailAddressTable);
    // add button panel
    dialogContent.setWidget(3, 0, dialogButtonPanel);
    dialogContent.getFlexCellFormatter().setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_CENTER);
    dialogContent.getCellFormatter().setVerticalAlignment(3, 0, HasVerticalAlignment.ALIGN_BOTTOM);
    dialogContent.getFlexCellFormatter().setColSpan(3, 0, 2);
    setWidget(dialogContent);
  }

  public void sendEmail(PermissibleObject object, String subject, String message, String fromAddress, String fromName, String toAddresses) {
    // replace {fromAddress} with fromAddress
    // replace {fromName} with fromName
    subject = subject.replace("{fromAddress}", fromAddress); //$NON-NLS-1$ 
    message = message.replace("{fromAddress}", fromAddress); //$NON-NLS-1$ 

    subject = subject.replace("{fromName}", fromName); //$NON-NLS-1$ 
    message = message.replace("{fromName}", fromName); //$NON-NLS-1$ 

    // replace {toAddress} with toAddress on server
    // replace {toName} with toName on server
    
    BaseServiceCache.getService().sendEmail(permissibleObject, subject, message, fromAddress, fromName, toAddresses, new AsyncCallback<Void>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(Void result) {
      }
    });

  }
}
