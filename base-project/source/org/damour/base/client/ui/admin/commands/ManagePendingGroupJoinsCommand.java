package org.damour.base.client.ui.admin.commands;

import org.damour.base.client.objects.User;
import org.damour.base.client.ui.admin.ManagePendingGroupMembershipsPanel;
import org.damour.base.client.ui.buttons.MenuButtonCommand;
import org.damour.base.client.ui.dialogs.PromptDialogBox;

public class ManagePendingGroupJoinsCommand implements MenuButtonCommand {

  User user;
  
  public ManagePendingGroupJoinsCommand(User user) {
    this.user = user;
  }

  public void execute() {
    popup.hide();
    PromptDialogBox promptDialog = new PromptDialogBox("Pending Group Memberships", "OK", null, null, false, true);
    ManagePendingGroupMembershipsPanel panel = new ManagePendingGroupMembershipsPanel(user, promptDialog);
    promptDialog.setContent(panel);
    promptDialog.center();
  }
}
