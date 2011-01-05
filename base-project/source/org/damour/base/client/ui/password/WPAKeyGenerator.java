package org.damour.base.client.ui.password;

import org.damour.base.client.ui.buttons.Button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

public class WPAKeyGenerator {

  ListBox lengthCombo = new ListBox(false);
  TextBox asciiText = new TextBox();
  TextBox hexText = new TextBox();

  public WPAKeyGenerator() {

    hexText.setVisibleLength(30);
    hexText.setWidth("450px");
    hexText.addFocusHandler(new FocusHandler() {
      
      public void onFocus(FocusEvent event) {
        hexText.selectAll();
      }
    });
    asciiText.setVisibleLength(30);
    asciiText.setWidth("250px");
    asciiText.addFocusHandler(new FocusHandler() {
      
      public void onFocus(FocusEvent event) {
        asciiText.selectAll();
      }
    });

    lengthCombo.addItem("160-bit WPA Key (minimum security)", "20");
    lengthCombo.addItem("504-bit WPA Key (maximum security)", "63");

    Button generateButton = new Button("Generate Key");
    generateButton.addClickHandler(new ClickHandler() {
      
      public void onClick(ClickEvent event) {
        int length = Integer.parseInt(lengthCombo.getValue(lengthCombo.getSelectedIndex()));
        String ascii = "";
        String hex = "";
        for (int i = 0; i < length; i++) {
          char character = PasswordConst.WEPCharacters[Random.nextInt(PasswordConst.WEPCharacters.length)];
          ascii += character;
          hex += Integer.toHexString(character);
        }
        asciiText.setText(ascii);
        hexText.setText(hex);
      }
    });

    Button generateCustomKeyButton = new Button("Use as Custom Phrase");
    generateCustomKeyButton.addClickHandler(new ClickHandler() {
      
      public void onClick(ClickEvent event) {
        String ascii = asciiText.getText();
        String hex = "";
        for (int i = 0; i < ascii.length(); i++) {
          char character = ascii.charAt(i);
          hex += Integer.toHexString(character);
        }
        hexText.setText(hex);
      }
    });

    FlexTable generatePanel = new FlexTable();
    generatePanel.setWidget(0, 0, new Label("Key size:", false));
    generatePanel.setWidget(0, 1, lengthCombo);
    generatePanel.setWidget(0, 2, generateButton);

    generatePanel.setWidget(1, 0, new Label("ASCII"));
    generatePanel.setWidget(1, 1, asciiText);
    generatePanel.setWidget(1, 2, generateCustomKeyButton);
    generatePanel.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);

    generatePanel.setWidget(2, 0, new Label("HEX"));
    generatePanel.setWidget(2, 1, hexText);
    generatePanel.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);
    generatePanel.getFlexCellFormatter().setColSpan(2, 1, 2);

    HorizontalPanel contentTable = new HorizontalPanel();
    contentTable.setStyleName("contentPanel");
    contentTable.add(generatePanel);

    HorizontalPanel applicationPanel = new HorizontalPanel();
    applicationPanel.setStyleName("applicationPanel");
    applicationPanel.add(contentTable);

    RootPanel rp = RootPanel.get("content");
    rp.add(applicationPanel);
  }
}
