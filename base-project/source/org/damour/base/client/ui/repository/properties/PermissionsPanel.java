package org.damour.base.client.ui.repository.properties;

import java.util.HashMap;
import java.util.List;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.SecurityPrincipal;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.authentication.AuthenticationHandler;
import org.damour.base.client.ui.buttons.Button;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.PromptDialogBox;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PermissionsPanel extends VerticalPanel {

  private PermissibleObject permissibleObject;
  private List<Permission> permissions;
  private ListBox principalListBox = new ListBox(false);
  private SimplePanel permissionPanel = new SimplePanel();
  private boolean showUserPerms = true;
  private boolean showGroupPerms = true;
  private HashMap<String, User> fetchedUsers = new HashMap<String, User>();
  private HashMap<String, UserGroup> fetchedGroups = new HashMap<String, UserGroup>();
  private boolean dirty = false;

  public PermissionsPanel(PermissibleObject permissibleObject, List<Permission> permissions, boolean showUserPerms, boolean showGroupPerms) {
    this.permissions = permissions;
    this.showUserPerms = showUserPerms;
    this.showGroupPerms = showGroupPerms;
    this.permissibleObject = permissibleObject;
    buildUI();
    populateUI();
  }

  public void buildUI() {
    principalListBox.addChangeHandler(new ChangeHandler() {

      public void onChange(ChangeEvent event) {
        populatePermissionPanel();
      }
    });
    principalListBox.setVisibleItemCount(6);
    principalListBox.setWidth("180px");

    VerticalPanel buttonPanel = new VerticalPanel();
    Button addButton = new Button("Add...");
    addButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        final VerticalPanel addPermissionPanel = new VerticalPanel();
        final ListBox addPrincipalListBox = new ListBox(true);
        addPrincipalListBox.setWidth("200px");
        addPrincipalListBox.setVisibleItemCount(12);
        addPermissionPanel.add(addPrincipalListBox);
        if (showUserPerms) {
          AsyncCallback<List<User>> getUsersCallback = new AsyncCallback<List<User>>() {
            public void onFailure(Throwable caught) {
              Window.alert(caught.getMessage());
            }

            public void onSuccess(List<User> users) {
              fetchedUsers.clear();
              for (User user : users) {
                addPrincipalListBox.addItem(user.getUsername());
                fetchedUsers.put(user.getUsername(), user);
              }
            }
          };
          BaseServiceCache.getService().getUsers(getUsersCallback);
        }
        if (showGroupPerms) {
          AsyncCallback<List<UserGroup>> getGroupsCallback = new AsyncCallback<List<UserGroup>>() {
            public void onFailure(Throwable caught) {
              Window.alert(caught.getMessage());
            }

            public void onSuccess(List<UserGroup> groups) {
              fetchedGroups.clear();
              for (UserGroup group : groups) {
                addPrincipalListBox.addItem(group.getName());
                fetchedGroups.put(group.getName(), group);
              }
            }
          };
          BaseServiceCache.getService().getGroups(AuthenticationHandler.getInstance().getUser(), getGroupsCallback);
        }

        PromptDialogBox dialog = new PromptDialogBox("Add New Permission", "OK", null, "Cancel", false, true);
        dialog.setContent(addPermissionPanel);
        dialog.setCallback(new IDialogCallback() {
          public void okPressed() {
            for (int i = 0; i < addPrincipalListBox.getItemCount(); i++) {
              if (addPrincipalListBox.isItemSelected(i)) {
                Permission newPerm = new Permission();
                newPerm.setPermissibleObject(permissibleObject);
                SecurityPrincipal principal = fetchedUsers.get(addPrincipalListBox.getItemText(i));
                if (principal == null) {
                  principal = fetchedGroups.get(addPrincipalListBox.getItemText(i));
                }
                newPerm.setSecurityPrincipal(principal);
                permissions.add(newPerm);
                dirty = true;
                populateUI();
                if (principalListBox.getItemCount() > 0) {
                  principalListBox.setSelectedIndex(principalListBox.getItemCount() - 1);
                }
                populatePermissionPanel();
              }
            }
          }

          public void cancelPressed() {
          }
        });
        dialog.center();
      }
    });

    Button removeButton = new Button("Remove...");
    removeButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        String principalName = principalListBox.getValue(principalListBox.getSelectedIndex());
        Permission permission = null;
        for (Permission mypermission : permissions) {
          if (showUserPerms && mypermission.getSecurityPrincipal() instanceof User) {
            User user = (User) mypermission.getSecurityPrincipal();
            if (principalName.equalsIgnoreCase(user.getUsername())) {
              permission = mypermission;
              break;
            }
          } else if (showGroupPerms && mypermission.getSecurityPrincipal() instanceof UserGroup) {
            UserGroup group = (UserGroup) mypermission.getSecurityPrincipal();
            if (principalName.equalsIgnoreCase(group.getName())) {
              permission = mypermission;
              break;
            }
          }
        }
        permissions.remove(permission);
        dirty = true;
        populateUI();
        if (principalListBox.getItemCount() > 0) {
          principalListBox.setSelectedIndex(principalListBox.getItemCount() - 1);
        }
        populatePermissionPanel();
      }
    });

    buttonPanel.add(addButton);
    buttonPanel.add(removeButton);

    HorizontalPanel principalPanel = new HorizontalPanel();
    principalPanel.setSpacing(5);
    principalPanel.add(principalListBox);
    principalPanel.add(buttonPanel);

    add(principalPanel);
    add(permissionPanel);
  }

  public void populatePermissionPanel() {
    permissionPanel.clear();
    if (principalListBox.getSelectedIndex() < 0) {
      return;
    }

    String principalName = principalListBox.getValue(principalListBox.getSelectedIndex());
    Permission tmppermission = null;
    for (Permission mypermission : permissions) {
      if (showUserPerms && mypermission.getSecurityPrincipal() instanceof User) {
        User user = (User) mypermission.getSecurityPrincipal();
        if (principalName.equalsIgnoreCase(user.getUsername())) {
          tmppermission = mypermission;
          break;
        }
      } else if (showGroupPerms && mypermission.getSecurityPrincipal() instanceof UserGroup) {
        UserGroup group = (UserGroup) mypermission.getSecurityPrincipal();
        if (principalName.equalsIgnoreCase(group.getName())) {
          tmppermission = mypermission;
          break;
        }
      }
    }
    final Permission permission = tmppermission;

    final CheckBox readPermCheckBox = new CheckBox("Read");
    final CheckBox writePermCheckBox = new CheckBox("Write");
    final CheckBox executePermCheckBox = new CheckBox("Execute");
    final CheckBox createChildrenPermCheckBox = new CheckBox("Create Children");
    readPermCheckBox.setValue(permission.isReadPerm());
    writePermCheckBox.setValue(permission.isWritePerm());
    executePermCheckBox.setValue(permission.isExecutePerm());
    createChildrenPermCheckBox.setValue(permission.isCreateChildPerm());

    readPermCheckBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        permission.setReadPerm(readPermCheckBox.getValue());
        dirty = true;
      }
    });
    writePermCheckBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        permission.setWritePerm(writePermCheckBox.getValue());
        dirty = true;
      }
    });
    executePermCheckBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        permission.setExecutePerm(executePermCheckBox.getValue());
        dirty = true;
      }
    });
    createChildrenPermCheckBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        permission.setCreateChildPerm(createChildrenPermCheckBox.getValue());
        dirty = true;
      }
    });
    CaptionPanel captionPanel = new CaptionPanel("Permissions");
    VerticalPanel checkboxPanel = new VerticalPanel();
    checkboxPanel.add(readPermCheckBox);
    checkboxPanel.add(writePermCheckBox);
    checkboxPanel.add(executePermCheckBox);
    checkboxPanel.add(createChildrenPermCheckBox);
    captionPanel.add(checkboxPanel);
    permissionPanel.add(captionPanel);
  }

  public void populateUI() {
    principalListBox.clear();
    for (Permission permission : permissions) {
      if (showUserPerms && permission.getSecurityPrincipal() instanceof User) {
        User user = (User) permission.getSecurityPrincipal();
        principalListBox.addItem(user.getUsername());
      } else if (showGroupPerms && permission.getSecurityPrincipal() instanceof UserGroup) {
        UserGroup group = (UserGroup) permission.getSecurityPrincipal();
        principalListBox.addItem(group.getName());
      }
    }
    populatePermissionPanel();
  }

  public List<Permission> getPermissions() {
    return permissions;
  }

  public void setPermissions(List<Permission> permissions) {
    this.permissions = permissions;
  }

  public void apply(final AsyncCallback<Void> callback) {
    if (dirty) {
      AsyncCallback<Void> setPermCallback = new AsyncCallback<Void>() {
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        public void onSuccess(Void nothing) {
        }
      };
      BaseServiceCache.getService().setPermissions(permissibleObject, permissions, setPermCallback);
    }
  }

}
