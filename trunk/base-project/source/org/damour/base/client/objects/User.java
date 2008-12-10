package org.damour.base.client.objects;

import java.io.Serializable;

public class User extends SecurityPrincipal implements Serializable, IHibernateFriendly {

  public String username;
  public transient String passwordHash;
  public String passwordHint;
  public String firstname;
  public String lastname;
  public String email;
  public long birthday;
  public long signupDate;
  
  public boolean administrator = false;
  public boolean validated = false;

  public User() {
  }

  public boolean isFieldUnique(String fieldName) {
    if (fieldName.equals("username")) {
      return true;
    }
    return false;
  }

  public boolean isFieldKey(String fieldName) {
    return false;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public String getPasswordHint() {
    return passwordHint;
  }

  public void setPasswordHint(String passwordHint) {
    this.passwordHint = passwordHint;
  }

  public long getSignupDate() {
    return signupDate;
  }

  public void setSignupDate(long signupDate) {
    this.signupDate = signupDate;
  }

  public long getBirthday() {
    return birthday;
  }

  public void setBirthday(long birthday) {
    this.birthday = birthday;
  }

  public boolean isAdministrator() {
    return administrator;
  }

  public void setAdministrator(boolean administrator) {
    this.administrator = administrator;
  }

  public boolean isValidated() {
    return validated;
  }

  public void setValidated(boolean validated) {
    this.validated = validated;
  }

}