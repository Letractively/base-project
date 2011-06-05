package org.damour.base.client.ui.repository.properties;

import java.util.ArrayList;
import java.util.List;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.tabs.BaseTabPanel;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PropertiesPanel extends VerticalPanel {

  public static enum VIEW {
    GENERAL, USERPERMS, GROUPPERMS
  };

  private PermissibleObject permissibleObject;
  private List<Permission> permissions;

  GeneralPanel generalPanel = null;
  PermissionsPanel userPermissionsPanel = null;
  PermissionsPanel groupPermissionsPanel = null;
  BaseTabPanel tabPanel = new BaseTabPanel();
  VIEW defaultView = VIEW.GENERAL;

  public PropertiesPanel(PermissibleObject permissibleObject, List<Permission> permissions, VIEW view) {
    this.permissibleObject = permissibleObject;
    this.permissions = permissions;
    this.defaultView = view;
    buildUI();
    if (permissions == null) {
      fetchPermissions();
    } else {
      populateUI();
    }
  }

  private void buildUI() {
    generalPanel = new GeneralPanel(permissibleObject);
    userPermissionsPanel = new PermissionsPanel(permissibleObject, new ArrayList<Permission>(), true, false);
    groupPermissionsPanel = new PermissionsPanel(permissibleObject, new ArrayList<Permission>(), false, true);

    tabPanel.setWidth("100%");
    tabPanel.setHeight("300px");
    add(tabPanel);
    setWidth("250px");
  }

  private void populateUI() {
    tabPanel.clear();
    tabPanel.addTab("General", "General", false, generalPanel);
    tabPanel.addTab("User Permissions", "User Permissions", false, userPermissionsPanel); 
    tabPanel.addTab("Groups Permissions", "Groups Permissions", false, groupPermissionsPanel);
    if (defaultView.equals(VIEW.GENERAL)) {
      tabPanel.selectTab(0);
    } else if (defaultView.equals(VIEW.USERPERMS)) {
      tabPanel.selectTab(1);
    } else if (defaultView.equals(VIEW.GROUPPERMS)) {
      tabPanel.selectTab(2);
    }
    userPermissionsPanel.setPermissions(permissions);
    userPermissionsPanel.populateUI();
    groupPermissionsPanel.setPermissions(permissions);
    groupPermissionsPanel.populateUI();
  }

  private void handleFetchFailure() {
    tabPanel.clear();
    tabPanel.addTab("General", "General", false, generalPanel);
    tabPanel.selectTab(0);
  }

  private void fetchPermissions() {
    AsyncCallback<List<Permission>> callback = new AsyncCallback<List<Permission>>() {
      public void onFailure(Throwable caught) {
        handleFetchFailure();
      }

      public void onSuccess(List<Permission> inPermissions) {
        permissions = inPermissions;
        populateUI();
      }
    };
    BaseServiceCache.getService().getPermissions(permissibleObject, callback);
  }

  public void apply(AsyncCallback<Void> callback) {
    generalPanel.apply(callback);
    userPermissionsPanel.apply(callback);
    groupPermissionsPanel.apply(callback);
  }

}
