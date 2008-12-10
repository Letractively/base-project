package org.damour.base.client.ui.admin.commands;

import org.damour.base.client.objects.User;
import org.damour.base.client.ui.admin.EditGroupsPanel;
import org.damour.base.client.ui.buttons.MenuButtonCommand;
import org.damour.base.client.ui.dialogs.PromptDialogBox;

public class ManageMyGroupsCommand implements MenuButtonCommand {

  User user;
  
  public ManageMyGroupsCommand(User user) {
    this.user = user;
  }

  public void execute() {
    popup.hide();
    EditGroupsPanel panel = new EditGroupsPanel(null, null, null, user, true, true);
    panel.setWidth("100%");
    PromptDialogBox promptDialog = new PromptDialogBox("Manage My Groups", "OK", null, null, false, true);
    promptDialog.setContent(panel);
    promptDialog.setWidth("640px");
    promptDialog.setHeight("200px");
    promptDialog.center();
  }
}
