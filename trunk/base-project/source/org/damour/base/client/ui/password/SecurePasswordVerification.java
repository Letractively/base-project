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

  private HTML strengthMeter = new HTML();
  private boolean simpleStyle = false;

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
      strengthMeter.setHTML("&nbsp;Strong " + (simpleStyle?"":score));
      if (!simpleStyle) {
        strengthMeter.setStyleName("password-strength-strong");
      } else {
        strengthMeter.removeStyleDependentName("moderate");
        strengthMeter.removeStyleDependentName("weak");
        strengthMeter.addStyleDependentName("strong");
      }
    } else if (score >= 65) {
      strengthMeter.setHTML("&nbsp;Moderate " + (simpleStyle?"":score));
      if (!simpleStyle) {
        strengthMeter.setStyleName("password-strength-moderate");
      } else {
        strengthMeter.removeStyleDependentName("strong");
        strengthMeter.removeStyleDependentName("weak");
        strengthMeter.addStyleDependentName("moderate");
      }
    } else {
      strengthMeter.setHTML("&nbsp;Weak " + (simpleStyle?"":score));
      if (!simpleStyle) {
        strengthMeter.setStyleName("password-strength-weak");
      } else {
        strengthMeter.removeStyleDependentName("moderate");
        strengthMeter.removeStyleDependentName("strong");
        strengthMeter.addStyleDependentName("weak");
      }
    }
  }

  public SecurePasswordVerification(boolean simpleStyle, final TextBox textBox, final TextBox confirmTextBox) {
    this.simpleStyle = simpleStyle;
    textBox.addKeyUpHandler(new KeyUpHandler() {

      public void onKeyUp(KeyUpEvent event) {
        verifyPasswordStrength(textBox.getText());
      }
    });

    strengthMeter.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        // bring up dialog to generate strong password
        SecurePasswordBuilder builder = new SecurePasswordBuilder();
        String password = builder.generatePassword();
        MessageDialogBox.alert("Generated Password", password);
        textBox.setText(password);
        verifyPasswordStrength(textBox.getText());
      }
    });
    strengthMeter.setTitle("Click to generate random secure password");
    if (!simpleStyle) {
      strengthMeter.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      strengthMeter.setHTML("&nbsp;");
      strengthMeter.setWidth("100px");
      strengthMeter.setStyleName("password-strength-empty");
    } else {
      strengthMeter.setStyleName("password-simple-style");
    }
    setStyleName("contentPanel");
    add(strengthMeter);
    verifyPasswordStrength(textBox.getText());
  }

}