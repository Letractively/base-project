package org.damour.base.client.ui.admin.commands;

import org.damour.base.client.objects.User;
import org.damour.base.client.ui.admin.EditGroupsForUserPanel;
import org.damour.base.client.ui.buttons.MenuButtonCommand;
import org.damour.base.client.ui.dialogs.PromptDialogBox;

public class JoinLeaveGroupsCommand implements MenuButtonCommand {
  
  User user;

  public JoinLeaveGroupsCommand(User user) {
    this.user = user;
  }

  public void execute() {
    popup.hide();
    final EditGroupsForUserPanel editGroupsForUserPanel = new EditGroupsForUserPanel(null, null, null, user);
    final PromptDialogBox editGroupDialogBox = new PromptDialogBox("Join/Leave Groups", "OK", null, null, false, true);
    editGroupDialogBox.setContent(editGroupsForUserPanel);
    editGroupDialogBox.center();
  }
}
