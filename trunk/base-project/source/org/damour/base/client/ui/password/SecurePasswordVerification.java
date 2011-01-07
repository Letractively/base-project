package org.damour.base.client.ui.password;

import org.damour.base.client.ui.dialogs.MessageDialogBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

public class SecurePasswordVerification extends HorizontalPanel {

  private HTML strengthMeterSegment1 = new HTML();

  public void verifyPasswordStrength(String password) {
    int score = 0;
    // is password at least 8 digits
    if (password.length() >= 8) {
      score += 20;
    }
    boolean containsNumber = false;
    for (int i = 0; i < PasswordConst.numbers.length; i++) {
      int index = password.indexOf(PasswordConst.numbers[i]);
      if (index >= 0 && index < password.length()) {
        containsNumber = true;
        break;
      }
    }
    if (containsNumber) {
      score += 20;
    }
    boolean containsLowerLetter = false;
    boolean containsUpperLetter = false;
    for (int i = 0; i < PasswordConst.letters.length; i++) {
      int lowerIndex = password.indexOf(PasswordConst.letters[i]);
      int upperIndex = password.indexOf(String.valueOf(PasswordConst.letters[i]).toUpperCase());
      if (lowerIndex >= 0 && lowerIndex < password.length()) {
        containsLowerLetter = true;
      }
      if (upperIndex >= 0 && upperIndex < password.length()) {
        containsUpperLetter = true;
      }
    }
    if (containsLowerLetter) {
      score += 10;
    }
    if (containsUpperLetter) {
      score += 10;
    }
    if (containsLowerLetter && containsUpperLetter) {
      score += 20;
    }
    // check that the thing contains a number
    boolean containsSymbol = false;
    for (int i = 0; i < PasswordConst.symbols.length; i++) {
      int index = password.indexOf(PasswordConst.symbols[i]);
      if (index >= 0 && index < password.length()) {
        containsSymbol = true;
        break;
      }
    }
    if (containsSymbol) {
      score += 20;
    }

    // 100 is a perfect score
    if (score == 100) {
      strengthMeterSegment1.setText("Strong " + score);
      strengthMeterSegment1.setStyleName("password-strength-strong");
    } else if (score >= 65) {
      strengthMeterSegment1.setText("Moderate " + score);
      strengthMeterSegment1.setStyleName("password-strength-moderate");
    } else {
      strengthMeterSegment1.setText("Weak " + score);
      strengthMeterSegment1.setStyleName("password-strength-weak");
    }
  }

  public SecurePasswordVerification(final TextBox textBox, final TextBox confirmTextBox) {
    textBox.addKeyUpHandler(new KeyUpHandler() {

      public void onKeyUp(KeyUpEvent event) {
        verifyPasswordStrength(textBox.getText());
      }
    });

    strengthMeterSegment1.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        // bring up dialog to generate strong password
        SecurePasswordBuilder builder = new SecurePasswordBuilder();
        String password = builder.generatePassword();
        MessageDialogBox.alert("Generated Password", password);
        textBox.setText(password);
        verifyPasswordStrength(textBox.getText());
      }
    });
    strengthMeterSegment1.setTitle("Click to generate random secure password");
    strengthMeterSegment1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    strengthMeterSegment1.setHTML("&nbsp;");
    strengthMeterSegment1.setStyleName("password-strength-empty");
    strengthMeterSegment1.setWidth("100px");
    add(strengthMeterSegment1);
    setStyleName("contentPanel");
    verifyPasswordStrength(textBox.getText());
  }

}