package org.damour.base.client.ui.admin.commands;

import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.ui.IGenericCallback;
import org.damour.base.client.ui.admin.EditGroupPanel;
import org.damour.base.client.ui.buttons.MenuButtonCommand;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.IDialogValidatorCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;

public class CreateGroupCommand implements MenuButtonCommand {

  private User user;
  private IGenericCallback<UserGroup> callback;

  public CreateGroupCommand(User user, IGenericCallback<UserGroup> callback) {
    this.user = user;
    this.callback = callback;
  }

  public void execute() {
    popup.hide();
    final UserGroup group = new UserGroup();
    group.setOwner(user);

    final EditGroupPanel editGroupPanel = new EditGroupPanel(null, callback, null, group, false, false);
    final PromptDialogBox editGroupDialogBox = new PromptDialogBox("Create New Group", "OK", null, "Cancel", false, true);
    editGroupDialogBox.setContent(editGroupPanel);
    editGroupDialogBox.setFocusWidget(editGroupPanel.getNameTextBox());
    editGroupDialogBox.setValidatorCallback(new IDialogValidatorCallback() {
      public boolean validate() {
        if (editGroupPanel.getNameTextBox().getText() == null || "".equals(editGroupPanel.getNameTextBox().getText())) {
          MessageDialogBox dialog = new MessageDialogBox("Error", "Enter a group name.", true, true, true);
          dialog.center();
          return false;
        }
        return true;
      }
    });
    editGroupDialogBox.setCallback(new IDialogCallback() {
      public void okPressed() {
        if (!editGroupPanel.apply()) {
          editGroupDialogBox.center();
        }
      }

      public void cancelPressed() {
      }
    });
    editGroupDialogBox.center();

  }
}
