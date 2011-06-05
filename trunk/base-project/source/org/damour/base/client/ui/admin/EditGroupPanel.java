package org.damour.base.client.ui.admin;

import java.util.List;

import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.IGenericCallback;
import org.damour.base.client.ui.buttons.Button;
import org.damour.base.client.ui.dialogs.MessageDialogBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class EditGroupPanel extends FlexTable {

  IGenericCallback<UserGroup> callback;
  IAdminCallback adminCallback;
  List<User> users;
  UserGroup group;
  boolean showApply = true;
  boolean showUsers = true;

  TextBox nameTextBox = new TextBox();
  TextBox descriptionTextBox = new TextBox();

  ListBox ownerListBox = new ListBox();
  CheckBox autoJoinCheckBox = new CheckBox("Auto-join");
  CheckBox lockGroupCheckBox = new CheckBox("Lock Group");
  CheckBox visibleCheckBox = new CheckBox("Visible");

  public EditGroupPanel(IAdminCallback adminCallback, final IGenericCallback<UserGroup> callback, final List<User> users, final UserGroup group, boolean showApply, boolean showUsers) {
    this.callback = callback;
    this.users = users;
    this.group = group;
    this.showApply = showApply;
    this.showUsers = showUsers;

    autoJoinCheckBox.setTitle("Allow users to join this group without your permission");
    lockGroupCheckBox.setTitle("Prevent users from joining and requesting to join this group");
    visibleCheckBox.setTitle("Hide this group from other users (keep it private)");
    
    if (users == null && showUsers) {
      fetchUsers();
    } else {
      populateUI();
    }

  }

  public void populateUI() {
    nameTextBox.setText(group.getName());
    descriptionTextBox.setText(group.getDescription());
    if (showUsers) {
      for (int i = 0; i < users.size(); i++) {
        User user = users.get(i);
        ownerListBox.addItem(user.getUsername());
        if (user.getId().equals(group.getOwner().getId())) {
          group.setOwner(user);
          ownerListBox.setSelectedIndex(i);
        }
      }
    }
    autoJoinCheckBox.setValue(group.isAutoJoin());
    lockGroupCheckBox.setValue(group.isLocked());
    visibleCheckBox.setValue(group.isVisible());

    Button applyButton = new Button("Apply");
    applyButton.addClickHandler(new ClickHandler() {
      
      public void onClick(ClickEvent event) {
        apply();
      }
    });
    applyButton.setTitle("Apply Changes");

    // build ui
    int row = 0;
    setWidget(row, 0, new Label("Name"));
    setWidget(row, 1, nameTextBox);
    setWidget(++row, 0, new Label("Description"));
    setWidget(row, 1, descriptionTextBox);
    
    if (showUsers) {
      setWidget(++row, 0, new Label("Owner"));
      setWidget(row, 1, ownerListBox);
    }
    setWidget(++row, 0, new Label());
    setWidget(row, 1, autoJoinCheckBox);
    setWidget(++row, 0, new Label());
    setWidget(row, 1, lockGroupCheckBox);
    setWidget(++row, 0, new Label());
    setWidget(row, 1, visibleCheckBox);

    if (showApply) {
      setWidget(++row, 0, new Label(""));
      setWidget(row, 1, applyButton);
    }
  }

  private void fetchUsers() {
    final AsyncCallback<List<User>> getUsersCallback = new AsyncCallback<List<User>>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(List<User> users) {
        EditGroupPanel.this.users = users;
        populateUI();
        if (adminCallback != null) {
          adminCallback.usersFetched(users);
        }
      };
    };
    BaseServiceCache.getService().getUsers(getUsersCallback);
  }

  public boolean apply() {
    if (nameTextBox.getText() == null || "".equals(nameTextBox.getText())) {
      MessageDialogBox dialog = new MessageDialogBox("Error", "Enter a group name.", true, true, true);
      dialog.center();
      return false;
    }

    group.setName(nameTextBox.getText());
    group.setDescription(descriptionTextBox.getText());
    if (showUsers) {
      for (User user : users) {
        if (user.getUsername().equals(ownerListBox.getItemText(ownerListBox.getSelectedIndex()))) {
          group.setOwner(user);
        }
      }
    }
    group.setAutoJoin(autoJoinCheckBox.getValue());
    group.setLocked(lockGroupCheckBox.getValue());
    group.setVisible(visibleCheckBox.getValue());

    final AsyncCallback<UserGroup> updateGroupCallback = new AsyncCallback<UserGroup>() {
      public void onFailure(Throwable caught) {
        MessageDialogBox dialog = new MessageDialogBox("Error", "Could not save group: " + caught.getMessage(), true, true, true);
        dialog.center();
      }

      public void onSuccess(UserGroup group) {
        if (group == null) {
          MessageDialogBox dialog = new MessageDialogBox("Error", "Could not save group.", true, true, true);
          dialog.center();
        } else if (callback != null) {
          callback.invoke(group);
        }
      };
    };

    BaseServiceCache.getService().createOrEditGroup(group, updateGroupCallback);
    return true;
  }

  public TextBox getNameTextBox() {
    return nameTextBox;
  }
}
