package org.damour.base.client.ui.authentication;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.damour.base.client.objects.User;
import org.damour.base.client.service.BaseServiceAsync;
import org.damour.base.client.ui.buttons.Button;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.IDialogValidatorCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.thapar.gwt.user.ui.client.widget.simpledatepicker.SimpleDatePicker;

public class AuthenticationHandler {

  private List<IAuthenticationListener> listeners = new ArrayList<IAuthenticationListener>();

  public String domain = "the website owner";

  private User user;
  TextBox username = new TextBox();
  PasswordTextBox password = new PasswordTextBox();
  PasswordTextBox passwordConfirm = new PasswordTextBox();
  TextBox passwordHint = new TextBox();
  TextBox emailAddress = new TextBox();
  TextBox firstname = new TextBox();
  TextBox lastname = new TextBox();
  CheckBox disclaimerCheckBox = new CheckBox("I have read and agree with the disclaimer statement");
  SimpleDatePicker birthdayPicker;

  final Button signupButton = new Button("Signup");
  PromptDialogBox loginDialog = new PromptDialogBox("Login", "Login", signupButton, "Cancel", false, true);
  PromptDialogBox accountDialog = new PromptDialogBox("Edit Account", "Submit", null, "Cancel", false, true);

  Label hintLink = new Label("Hint");

  Button readDisclaimer = new Button("View Disclaimer");

  Image captchaValidationImage = new Image();
  TextBox captchaValidationTextBox = new TextBox();

  private static AuthenticationHandler instance;

  public static AuthenticationHandler getInstance() {
    if (instance == null) {
      instance = new AuthenticationHandler();
    }
    return instance;
  }

  public static AuthenticationHandler getNewInstance() {
    return new AuthenticationHandler();
  }

  private AuthenticationHandler() {

    loginDialog.setContent(new FlexTable());

    final Date possibleBirthday = new Date();
    possibleBirthday.setYear(possibleBirthday.getYear() - 25);
    birthdayPicker = new SimpleDatePicker(possibleBirthday);
    username.setVisibleLength(30);
    password.setVisibleLength(30);
    password.addFocusListener(new FocusListener() {
      public void onFocus(Widget sender) {
        password.selectAll();
      }

      public void onLostFocus(Widget sender) {
      }
    });

    passwordConfirm.setVisibleLength(30);
    passwordConfirm.addFocusListener(new FocusListener() {
      public void onFocus(Widget sender) {
        passwordConfirm.selectAll();
      }

      public void onLostFocus(Widget sender) {
      }
    });

    loginDialog.setValidatorCallback(new IDialogValidatorCallback() {
      public boolean validate() {
        boolean valid = true;
        String validationMessage = "";
        if (username == null || "".equals(username.getText())) {
          validationMessage += "<BR>You must enter a username.";
          valid = false;
        }
        if (password == null || "".equals(password.getText())) {
          validationMessage += "<BR>You must enter a password.";
          valid = false;
        }
        if (!valid) {
          final MessageDialogBox dialog = new MessageDialogBox("Validation Failed", validationMessage, true, true, true);
          dialog.center();
        }
        return valid;
      }
    });

    loginDialog.setCallback(new IDialogCallback() {
      public void okPressed() {
        login(username.getText(), password.getText());
      }

      public void cancelPressed() {
      }
    });

    signupButton.addClickListener(new ClickListener() {
      public void onClick(final Widget sender) {
        loginDialog.hide();
        // create an account, prepopulate UI with values from login dialog (user/pass)
        showNewAccountDialog(true);
      }
    });

    hintLink.setTitle("Retrieve your password hint.");
    hintLink.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    hintLink.setStyleName("link");
    hintLink.addClickListener(new ClickListener() {
      public void onClick(final Widget sender) {
        if (username.getText() == null || "".equals(username.getText())) {
          final MessageDialogBox dialog = new MessageDialogBox("Error", "Enter your username.", true, true, true);
          dialog.center();
        } else {
          getPasswordHint(username.getText());
        }
      }
    });
    readDisclaimer.addClickListener(new ClickListener() {

      public void onClick(final Widget sender) {
        String disclaimerText = new String(
            "By using this website, you acknowledge that you have read and agree to these terms.  Department staff may revise these terms periodically.  If you continue to use this website after changes are made to these terms, it will mean that you accept such changes.  If at any time you do not wish to accept the Terms, you may choose not to use this website.");
        disclaimerText += "<BR><BR>The information contained in this website is for general information purposes only. The information is provided by "
            + domain
            + " and whilst we endeavour to keep the information up-to-date and correct, we make no representations or warranties of any kind, express or implied, about the completeness, accuracy, reliability, suitability or availability with respect to the website or the information, products, services, or related graphics contained on the website for any purpose. Any reliance you place on such information is therefore strictly at your own risk.";
        disclaimerText += "<BR><BR>In no event will we be liable for any loss or damage including without limitation, indirect or consequential loss or damage, or any loss or damage whatsoever arising from loss of data or profits arising out of, or in connection with, the use of this website.";
        disclaimerText += "<BR><BR>Through this website you are able to link to other websites which are not under the control of " + domain
            + ". We have no control over the nature, content and availability of those sites. The inclusion of any links does not necessarily imply a recommendation or endorse the views expressed within them.";
        disclaimerText += "<BR><BR>Every effort is made to keep the website up and running smoothly. However, " + domain
            + " takes no responsibility for, and will not be liable for, the website being temporarily unavailable due to technical issues beyond our control.";
        final MessageDialogBox dialog = new MessageDialogBox("Disclaimer & Terms of Use", disclaimerText, true, true, true);
        dialog.setWidth("600px");
        dialog.center();
      }

    });
    disclaimerCheckBox.setTitle("You must read and agree in order to continue.");
    // present account dialog
    accountDialog.setText("New Account");

    passwordHint.setTitle("Give yourself a hint in case you forget your password");
    passwordHint.setVisibleLength(40);

    emailAddress.setTitle("Enter your email address");
    emailAddress.setVisibleLength(40);

    firstname.setTitle("Enter your firstname");
    lastname.setTitle("Enter your lastname");

    captchaValidationImage.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        createCaptchaImage();
      }
    });
  }
  public void showLoginDialog(boolean forceRecenter) {
    username.setReadOnly(false);
    // present login dialog
    final FlexTable contentPanel = (FlexTable) loginDialog.getContent();
    int row = 0;
    contentPanel.setWidget(row++, 0, new HTML("&nbsp;"));
    contentPanel.setWidget(row, 0, new Label("To sign on, enter your username and password below.", true));
    contentPanel.getFlexCellFormatter().setColSpan(row++, 0, 3);
    contentPanel.setWidget(row, 0, new HTML("&nbsp;"));
    contentPanel.getFlexCellFormatter().setColSpan(row++, 0, 3);
    Label usernameLabel = new Label("Username:");
    usernameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    contentPanel.setWidget(row, 0, usernameLabel);
    contentPanel.setWidget(row, 1, username);
    contentPanel.setWidget(row, 2, hintLink);
    contentPanel.getCellFormatter().setHorizontalAlignment(row++, 2, HasHorizontalAlignment.ALIGN_LEFT);
    Label passwordLabel = new Label("Password:");
    passwordLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    contentPanel.setWidget(row, 0, passwordLabel);
    contentPanel.setWidget(row, 1, password);
    contentPanel.setWidget(row, 2, new HTML("&nbsp;"));

    if (forceRecenter) {
      loginDialog.center();
    }
    username.setFocus(true);
  }

  public void getPasswordHint(final String username) {
    final AsyncCallback<String> callback = new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        final MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), true, true, true);
        dialog.center();
      }

      public void onSuccess(String hint) {
        if (hint == null) {
          MessageDialogBox dialog = new MessageDialogBox("Error", "Could not retrieve password hint.", true, true, true);
          dialog.center();
        } else {
          MessageDialogBox dialog = new MessageDialogBox("Password Hint", "Your password hint is: <b>" + hint + "</b>", true, true, true);
          dialog.center();
        }
      };
    };
    BaseServiceAsync.service.getLoginHint(username, callback);
  }

  public void showNewAccountDialog(final boolean showLoginIfCancelPressed) {
    username.setReadOnly(false);

    final HTML usernameLabel = new HTML("<b>Username</b>");
    usernameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML passwordLabel = new HTML("<b>Password</b>");
    passwordLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML passwordConfirmLabel = new HTML("<b>Confirm Password</b>");
    passwordConfirmLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML passwordHintLabel = new HTML("Password Hint");
    passwordHintLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML firstnameLabel = new HTML("Firstname");
    firstnameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML lastnameLabel = new HTML("Lastname");
    lastnameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML emailLabel = new HTML("<b>Email</b>");
    emailLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML birthdayLabel = new HTML("<b>Birthday</b>");
    birthdayLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final FlexTable contentPanel = new FlexTable();
    int row = 0;
    contentPanel.setWidget(row, 0, new HTML("&nbsp;"));
    contentPanel.getFlexCellFormatter().setColSpan(row++, 0, 2);
    contentPanel.setWidget(row, 0, usernameLabel);
    contentPanel.setWidget(row++, 1, username);
    contentPanel.setWidget(row, 0, passwordLabel);
    contentPanel.setWidget(row++, 1, password);
    contentPanel.setWidget(row, 0, passwordConfirmLabel);
    contentPanel.setWidget(row++, 1, passwordConfirm);
    contentPanel.setWidget(row, 0, passwordHintLabel);
    contentPanel.setWidget(row++, 1, passwordHint);
    contentPanel.setWidget(row, 0, firstnameLabel);
    contentPanel.setWidget(row++, 1, firstname);
    contentPanel.setWidget(row, 0, lastnameLabel);
    contentPanel.setWidget(row++, 1, lastname);
    contentPanel.setWidget(row, 0, emailLabel);
    contentPanel.setWidget(row++, 1, emailAddress);
    contentPanel.setWidget(row, 0, birthdayLabel);
    contentPanel.setWidget(row++, 1, birthdayPicker);

    contentPanel.setWidget(row++, 1, new HTML("<HR>"));
    contentPanel.setText(row++, 1, "Type the characters you see in the picture below.");
    createCaptchaImage();
    captchaValidationImage.setTitle("Click to load a new validation image");
    captchaValidationImage.setStyleName("captchaImage");
    contentPanel.setWidget(row++, 1, captchaValidationImage);
    contentPanel.setWidget(row++, 1, captchaValidationTextBox);
    contentPanel.setWidget(row++, 1, new HTML("<HR>"));
    contentPanel.setWidget(row++, 1, readDisclaimer);
    contentPanel.setWidget(row++, 1, disclaimerCheckBox);

    accountDialog.setCallback(new IDialogCallback() {
      public void okPressed() {
        boolean validationFailed = false;
        String validationMessage = "";
        if (username.getText() == null || "".equals(username.getText())) {
          validationMessage += "You must enter a username.<BR>";
          validationFailed = true;
        }
        if (password.getText() == null || "".equals(password.getText())) {
          validationMessage += "You must enter a password and a confirmation password.<BR>";
          validationFailed = true;
        }
        if (!password.getText().equals(passwordConfirm.getText())) {
          validationMessage += "Passwords do not match.<BR>";
          validationFailed = true;
        }
        if (emailAddress.getText() == null || "".equals(emailAddress.getText()) || emailAddress.getText().indexOf("@") == -1) {
          validationMessage += "You must enter a valid email address.<BR>";
          validationFailed = true;
        }
        if (birthdayPicker.getSelectedDate() == null) {
          validationMessage += "You must enter your birthdate.<BR>";
          validationFailed = true;
        }
        if (!disclaimerCheckBox.isChecked()) {
          validationMessage += "You must read and agree with the disclaimer statement to continue.<BR>";
          validationFailed = true;
        }
        if (validationFailed) {
          final MessageDialogBox dialog = new MessageDialogBox("Validation Failed", validationMessage, true, true, true);
          dialog.setCallback(new IDialogCallback() {
            public void okPressed() {
              accountDialog.center();
            }

            public void cancelPressed() {
            }
          });
          dialog.center();
          return;
        }
        createNewAccount(username.getText(), firstname.getText(), lastname.getText(), password.getText(), passwordHint.getText(), emailAddress.getText(), birthdayPicker.getSelectedDate().getTime());
      }

      public void cancelPressed() {
        if (showLoginIfCancelPressed) {
          showLoginDialog(true);
        }
      }
    });

    accountDialog.setContent(contentPanel);
    accountDialog.setText("New Account");
    accountDialog.center();
    username.setFocus(true);
  }

  private void createCaptchaImage() {
      captchaValidationImage.setUrl("/servlet/CaptchaImageGeneratorServlet?attempt=" + System.currentTimeMillis());
  }

  public void showEditAccountDialog(final User user) {
    this.user = user;
    password.setText("");
    passwordConfirm.setText("");
    username.setReadOnly(true);

    final HTML usernameLabel = new HTML("<b>Username</b>");
    usernameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML passwordLabel = new HTML("<b>Password</b>");
    passwordLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML passwordConfirmLabel = new HTML("<b>Confirm Password</b>");
    passwordConfirmLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML passwordHintLabel = new HTML("Password Hint");
    passwordHintLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML firstnameLabel = new HTML("Firstname");
    firstnameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML lastnameLabel = new HTML("Lastname");
    lastnameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML emailLabel = new HTML("<b>Email</b>");
    emailLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML birthdayLabel = new HTML("<b>Birthday</b>");
    birthdayLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final FlexTable contentPanel = new FlexTable();
    int row = 0;
    contentPanel.setWidget(row, 0, new HTML("&nbsp;"));
    contentPanel.getFlexCellFormatter().setColSpan(row++, 0, 2);
    contentPanel.setWidget(row, 0, usernameLabel);
    contentPanel.setWidget(row++, 1, username);
    username.setText(user.getUsername());
    contentPanel.setWidget(row, 0, passwordLabel);
    contentPanel.setWidget(row++, 1, password);
    contentPanel.setWidget(row, 0, passwordConfirmLabel);
    contentPanel.setWidget(row++, 1, passwordConfirm);
    contentPanel.setWidget(row, 0, passwordHintLabel);
    contentPanel.setWidget(row++, 1, passwordHint);
    passwordHint.setText(user.getPasswordHint());
    contentPanel.setWidget(row, 0, firstnameLabel);
    contentPanel.setWidget(row++, 1, firstname);
    firstname.setText(user.getFirstname());
    contentPanel.setWidget(row, 0, lastnameLabel);
    contentPanel.setWidget(row++, 1, lastname);
    lastname.setText(user.getLastname());
    contentPanel.setWidget(row, 0, emailLabel);
    contentPanel.setWidget(row++, 1, emailAddress);
    emailAddress.setText(user.getEmail());
    contentPanel.setWidget(row, 0, birthdayLabel);

    Date date = new Date(user.getBirthday());
    birthdayPicker = new SimpleDatePicker(date);
    birthdayPicker.setSelectedDate(date);
    birthdayPicker.setCurrentDate(date);
    birthdayPicker.setText(birthdayPicker.getDateFormatter().formatDate(date));
    contentPanel.setWidget(row++, 1, birthdayPicker);

    accountDialog.setCallback(new IDialogCallback() {
      public void okPressed() {
        boolean validationFailed = false;
        String validationMessage = "";

        if (password.getText() != null && !"".equals(password.getText()) && !password.getText().equals(passwordConfirm.getText())) {
          validationMessage += "<BR>You must enter a matching password and confirmation password.";
          validationFailed = true;
        }
        if (emailAddress.getText() == null || "".equals(emailAddress.getText()) || emailAddress.getText().indexOf("@") == -1) {
          validationMessage += "<BR>You must enter a valid email address.";
          validationFailed = true;
        }
        if (birthdayPicker.getSelectedDate() == null) {
          validationMessage += "<BR>You must enter your birthdate.";
          validationFailed = true;
        }
        if (validationFailed) {
          final MessageDialogBox dialog = new MessageDialogBox("Validation Failed", validationMessage, true, true, true);
          dialog.center();
          return;
        }
        user.setUsername(username.getText());
        user.setFirstname(firstname.getText());
        user.setLastname(lastname.getText());
        user.setPasswordHint(passwordHint.getText());
        user.setEmail(emailAddress.getText());
        user.setBirthday(birthdayPicker.getSelectedDate().getTime());
        editAccount(user, password.getText());
      }

      public void cancelPressed() {
      }
    });

    accountDialog.setContent(contentPanel);
    accountDialog.setText("Edit Account Settings");
    accountDialog.center();
    username.setFocus(true);
  }

  public void login(final String username, final String password) {
    final AsyncCallback<User> loginCallback = new AsyncCallback<User>() {
      public void onFailure(Throwable caught) {
        MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), true, true, true);
        dialog.setCallback(new IDialogCallback() {
          public void okPressed() {
            loginDialog.center();
          }

          public void cancelPressed() {
          }
        });
        dialog.center();
      }

      public void onSuccess(User user) {
        AuthenticationHandler.this.user = user;
        if (user == null) {
          MessageDialogBox dialog = new MessageDialogBox("Error", "Invalid username or password.", true, true, true);
          dialog.center();
        } else {
          loginDialog.hide();
          fireSetAuthenticatedUser(user);
        }
      };
    };
    BaseServiceAsync.service.login(username, password, loginCallback);
  }

  public void createNewAccount(final String username, final String firstname, final String lastname, final String password, final String passwordHint, final String email, final long birthday) {
    final AsyncCallback<User> loginCallback = new AsyncCallback<User>() {
      public void onFailure(Throwable caught) {
        MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), true, true, true);
        dialog.setCallback(new IDialogCallback() {
          public void okPressed() {
            accountDialog.center();
          }

          public void cancelPressed() {
          }
        });
        dialog.center();
      }

      public void onSuccess(User user) {
        AuthenticationHandler.this.user = user;
        if (user == null) {
          MessageDialogBox dialog = new MessageDialogBox("Error", "Could not create new account.  Try entering a different username.", true, true, true);
          dialog.center();
        } else {
          accountDialog.hide();
          fireSetAuthenticatedUser(user);
        }
      };
    };
    User user = new User();
    user.setUsername(username);
    user.setFirstname(firstname);
    user.setLastname(lastname);
    user.setPasswordHint(passwordHint);
    user.setEmail(email);
    user.setBirthday(birthday);
    BaseServiceAsync.service.createOrEditAccount(user, password, captchaValidationTextBox.getText().toUpperCase(), loginCallback);
  }

  public void editAccount(User user, String password) {
    final AsyncCallback<User> loginCallback = new AsyncCallback<User>() {
      public void onFailure(Throwable caught) {
        MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), true, true, true);
        dialog.setCallback(new IDialogCallback() {
          public void okPressed() {
            accountDialog.center();
          }

          public void cancelPressed() {
          }
        });
        dialog.center();
      }

      public void onSuccess(User user) {
        AuthenticationHandler.this.user = user;
        if (user == null) {
          MessageDialogBox dialog = new MessageDialogBox("Error", "Could not edit account.", true, true, true);
          dialog.center();
        } else {
          accountDialog.hide();
          fireSetAuthenticatedUser(user);
        }
      };
    };

    BaseServiceAsync.service.createOrEditAccount(user, password, null, loginCallback);
  }

  public void logout() {
    final AsyncCallback loginCallback = new AsyncCallback() {
      public void onFailure(Throwable caught) {
        Window.open("/", "_top", "");
      }

      public void onSuccess(Object result) {
        AuthenticationHandler.this.user = null;
        fireLoggedOut();
      };
    };
    BaseServiceAsync.service.logout(loginCallback);
  }

  public void handleUserAuthentication(final boolean forcePrompt) {
    final AsyncCallback<User> isAuthenticatedCallback = new AsyncCallback<User>() {
      public void onFailure(Throwable caught) {
        if (forcePrompt) {
          // try to login and get data
          showLoginDialog(true);
        }
      }

      public void onSuccess(User user) {
        AuthenticationHandler.this.user = user;
        if (user == null && forcePrompt) {
          // try to login and get data
          showLoginDialog(true);
        } else {
          fireSetAuthenticatedUser(user);
        }
      };
    };
    BaseServiceAsync.service.getAuthenticatedUser(isAuthenticatedCallback);
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  // LoginListener events
  public void fireLoggedOut() {
    for (IAuthenticationListener listener : listeners) {
      listener.loggedOut();
    }
  }

  public void fireSetAuthenticatedUser(User user) {
    for (IAuthenticationListener listener : listeners) {
      listener.setAuthenticatedUser(user);
    }
  }

  public void addLoginListener(IAuthenticationListener listener) {
    listeners.add(listener);
  }

  public void removeLoginListener(IAuthenticationListener listener) {
    listeners.remove(listener);
  }

  /**
   * @return the user
   */
  public User getUser() {
    return user;
  }

  /**
   * @param user
   *          the user to set
   */
  public void setUser(User user) {
    this.user = user;
  }

}
