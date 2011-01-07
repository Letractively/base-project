package org.damour.base.client.ui.password;

import org.damour.base.client.ui.buttons.Button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

public class SecurePasswordBuilder {

  ListBox lengthCombo = new ListBox(false);
  ListBox startWithCombo = new ListBox(false);
  ListBox endWithCombo = new ListBox(false);
  TextBox dontUseTextBox = new TextBox();
  RadioButton upperCase = new RadioButton("case");
  RadioButton lowerCase = new RadioButton("case");
  RadioButton mixedCase = new RadioButton("case");
  CheckBox useSymbolsCheckBox = new CheckBox("Use Symbols");
  CheckBox useNumbersCheckBox = new CheckBox("Use Numbers");
  CheckBox useLettersCheckBox = new CheckBox("Use Letters");
  TextBox generatedPasswordTextBox = new TextBox();

  public SecurePasswordBuilder() {
    for (int i = 4; i <= 25; i++) {
      lengthCombo.addItem("" + i);
    }
    lengthCombo.setSelectedIndex(6);

    startWithCombo.addItem("Upper-case Letter");
    startWithCombo.addItem("Lower-case Letter");
    startWithCombo.addItem("Number");
    startWithCombo.addItem("Symbol");
    startWithCombo.setSelectedIndex(1);
    endWithCombo.addItem("Upper-case Letter");
    endWithCombo.addItem("Lower-case Letter");
    endWithCombo.addItem("Number");
    endWithCombo.addItem("Symbol");
    endWithCombo.setSelectedIndex(0);

    upperCase.setText("Upper-case");
    lowerCase.setText("Lower-case");
    mixedCase.setText("Mixed-case");
    mixedCase.setValue(true);

    useSymbolsCheckBox.setValue(true);
    useNumbersCheckBox.setValue(true);
    useLettersCheckBox.setValue(true);
    useLettersCheckBox.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        upperCase.setEnabled(useLettersCheckBox.getValue());
        lowerCase.setEnabled(useLettersCheckBox.getValue());
        mixedCase.setEnabled(useLettersCheckBox.getValue());
      }

    });

    FlexTable contentTable = new FlexTable();
    contentTable.setStyleName("contentPanel");
    contentTable.setWidget(0, 0, new Label("Length"));
    contentTable.setWidget(0, 1, lengthCombo);
    contentTable.setWidget(0, 2, useSymbolsCheckBox);

    contentTable.setWidget(1, 0, new Label("Start With"));
    contentTable.setWidget(1, 1, startWithCombo);
    contentTable.setWidget(1, 2, useNumbersCheckBox);

    contentTable.setWidget(2, 0, new Label("End With"));
    contentTable.setWidget(2, 1, endWithCombo);
    contentTable.setWidget(2, 2, useLettersCheckBox);

    contentTable.setWidget(3, 0, new Label("Don't Use:"));
    contentTable.setWidget(3, 1, dontUseTextBox);
    contentTable.setWidget(3, 2, mixedCase);

    contentTable.setWidget(4, 2, upperCase);
    contentTable.setWidget(5, 2, lowerCase);

    generatedPasswordTextBox.setTextAlignment(TextBox.ALIGN_RIGHT);
    generatedPasswordTextBox.setVisibleLength(35);
    generatedPasswordTextBox.setMaxLength(25);
    generatedPasswordTextBox.addFocusHandler(new FocusHandler() {
      public void onFocus(FocusEvent event) {
        generatedPasswordTextBox.selectAll();
      }
    });
    Button generatePasswordButton = new Button("Generate!");
    generatePasswordButton.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        generatedPasswordTextBox.setText(generatePassword());
      }

    });

    FlexTable generatePanel = new FlexTable();
    generatePanel.setWidget(0, 0, generatePasswordButton);
    generatePanel.setWidget(0, 1, generatedPasswordTextBox);

    contentTable.setWidget(6, 0, generatePanel);
    contentTable.getFlexCellFormatter().setColSpan(6, 0, 3);

    HorizontalPanel applicationPanel = new HorizontalPanel();
    applicationPanel.setStyleName("applicationPanel");
    applicationPanel.add(contentTable);

    RootPanel rp = RootPanel.get("content");
    rp.add(applicationPanel);
  }

  public String generatePassword() {
    String generatedPassword = "";
    do {
      generatedPassword = "";
      // get first digit
      if (startWithCombo.getSelectedIndex() == 0) {
        // start with uppercase
        generatedPassword += getRandomCharacter(true);
      } else if (startWithCombo.getSelectedIndex() == 1) {
        // start with lower
        generatedPassword += getRandomCharacter(false);
      } else if (startWithCombo.getSelectedIndex() == 2) {
        // start with number
        generatedPassword += getRandomNumber();
      } else if (startWithCombo.getSelectedIndex() == 3) {
        // start with symbol
        generatedPassword += getRandomSymbol();
      }

      // build the meat of the password
      int length = Integer.parseInt(lengthCombo.getValue(lengthCombo.getSelectedIndex()));
      for (int i = 1; i < length - 1; i++) {
        if (useLettersCheckBox.getValue() && useNumbersCheckBox.getValue() && useSymbolsCheckBox.getValue()) {
          int random = Random.nextInt(3);
          if (random == 0) {
            // letter
            boolean useUpperCase = upperCase.getValue();
            if (lowerCase.getValue()) {
              useUpperCase = false;
            } else if (mixedCase.getValue()) {
              useUpperCase = Random.nextBoolean();
            }
            generatedPassword += getRandomCharacter(useUpperCase);
          } else if (random == 1) {
            // number
            generatedPassword += getRandomNumber();
          } else if (random == 2) {
            // symbol
            generatedPassword += getRandomSymbol();
          }
        } else if (useLettersCheckBox.getValue() && useNumbersCheckBox.getValue()) {
          boolean useLetters = Random.nextBoolean();
          if (useLetters) {
            boolean useUpperCase = upperCase.getValue();
            if (lowerCase.getValue()) {
              useUpperCase = false;
            } else if (mixedCase.getValue()) {
              useUpperCase = Random.nextBoolean();
            }
            generatedPassword += getRandomCharacter(useUpperCase);
          } else {
            generatedPassword += getRandomNumber();
          }
        } else if (useLettersCheckBox.getValue() && useSymbolsCheckBox.getValue()) {
          boolean useLetters = Random.nextBoolean();
          if (useLetters) {
            boolean useUpperCase = upperCase.getValue();
            if (lowerCase.getValue()) {
              useUpperCase = false;
            } else if (mixedCase.getValue()) {
              useUpperCase = Random.nextBoolean();
            }
            generatedPassword += getRandomCharacter(useUpperCase);
          } else {
            generatedPassword += getRandomSymbol();
          }
        } else if (useNumbersCheckBox.getValue() && useSymbolsCheckBox.getValue()) {
          boolean useNumbers = Random.nextBoolean();
          if (useNumbers) {
            generatedPassword += getRandomNumber();
          } else {
            generatedPassword += getRandomSymbol();
          }
        } else if (useLettersCheckBox.getValue()) {
          boolean useUpperCase = upperCase.getValue();
          if (lowerCase.getValue()) {
            useUpperCase = false;
          } else if (mixedCase.getValue()) {
            useUpperCase = Random.nextBoolean();
          }
          generatedPassword += getRandomCharacter(useUpperCase);
        } else if (useNumbersCheckBox.getValue()) {
          generatedPassword += getRandomNumber();
        } else if (useSymbolsCheckBox.getValue()) {
          generatedPassword += getRandomSymbol();
        }
      }

      // get last digit
      if (endWithCombo.getSelectedIndex() == 0) {
        // start with uppercase
        generatedPassword += getRandomCharacter(true);
      } else if (endWithCombo.getSelectedIndex() == 1) {
        // start with lower
        generatedPassword += getRandomCharacter(false);
      } else if (endWithCombo.getSelectedIndex() == 2) {
        // start with number
        generatedPassword += getRandomNumber();
      } else if (endWithCombo.getSelectedIndex() == 3) {
        // start with symbol
        generatedPassword += getRandomSymbol();
      }
    } while (!accept(generatedPassword, dontUseTextBox.getText()));
    return generatedPassword;
  }

  public boolean accept(String password, String dontUse) {
    boolean accept = true;
    for (int i = 0; i < dontUse.length(); i++) {
      if (password.indexOf(dontUse.charAt(i)) != -1) {
        accept = false;
        break;
      }
    }
    if (accept) {
      // make sure each of the desired categories are in use
      if (useNumbersCheckBox.getValue()) {
        // check that the thing contains a number
        boolean containsNumber = false;
        for (int i = 0; i < PasswordConst.numbers.length; i++) {
          int index = password.indexOf(PasswordConst.numbers[i]);
          if (index > 0 && index < password.length() - 2) {
            containsNumber = true;
            break;
          }
        }
        if (!containsNumber) {
          accept = false;
        }
      }
      // accept letter
      if (useLettersCheckBox.getValue()) {
        // check that the thing contains a number
        boolean containsLowerLetter = false;
        boolean containsUpperLetter = false;
        for (int i = 0; i < PasswordConst.letters.length; i++) {
          int lowerIndex = password.indexOf(PasswordConst.letters[i]);
          int upperIndex = password.indexOf(String.valueOf(PasswordConst.letters[i]).toUpperCase());
          if (lowerIndex > 0 && lowerIndex < password.length() - 2) {
            containsLowerLetter = true;
          }
          if (upperIndex > 0 && upperIndex < password.length() - 2) {
            containsUpperLetter = true;
          }
        }
        if (mixedCase.getValue() && (!containsLowerLetter || !containsUpperLetter)) {
          accept = false;
        }
        if (lowerCase.getValue() && !containsLowerLetter) {
          accept = false;
        }
        if (upperCase.getValue() && !containsUpperLetter) {
          accept = false;
        }
        if (!containsLowerLetter && !containsUpperLetter) {
          accept = false;
        }
      }
      // accept symbols
      if (useSymbolsCheckBox.getValue()) {
        // check that the thing contains a number
        boolean containsSymbol = false;
        for (int i = 0; i < PasswordConst.symbols.length; i++) {
          int index = password.indexOf(PasswordConst.symbols[i]);
          if (index > 0 && index < password.length() - 2) {
            containsSymbol = true;
            break;
          }
        }
        if (!containsSymbol) {
          accept = false;
        }
      }
    }
    return accept;
  }

  public char getRandomSymbol() {
    int random = Random.nextInt(PasswordConst.symbols.length);
    return PasswordConst.symbols[random];
  }

  public char getRandomNumber() {
    int random = Random.nextInt(PasswordConst.numbers.length);
    return PasswordConst.numbers[random];
  }

  public char getRandomCharacter(boolean upperCase) {
    // 0-25 = a-z
    int random = Random.nextInt(PasswordConst.letters.length);
    String character = new String("" + PasswordConst.letters[random]);
    if (upperCase) {
      character = character.toUpperCase();
    }
    return character.charAt(0);
  }
}
