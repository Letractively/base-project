package org.damour.base.client.ui.admin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.damour.base.client.objects.PendingGroupMembership;
import org.damour.base.client.objects.User;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.buttons.Button;
import org.damour.base.client.ui.dialogs.MessageDialogBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ManagePendingGroupMembershipsPanel extends VerticalPanel {

  private User user;
  private FlexTable pendingGroupMembersTable;
  private PopupPanel parentPopup;
  private HashMap<Integer, PendingGroupMembership> tableRowToPGM = new HashMap<Integer, PendingGroupMembership>();
  private Set<PendingGroupMembership> selectedPGMSet = new HashSet<PendingGroupMembership>();

  public ManagePendingGroupMembershipsPanel(User user, PopupPanel parentPopup) {
    this.user = user;
    this.parentPopup = parentPopup;
    fetchPendingGroupMemberships();
  }

  private void populateUI(List<PendingGroupMembership> pendingList) {
    clear();
    pendingGroupMembersTable = new FlexTable();
    pendingGroupMembersTable.setStyleName("baseTable");
    pendingGroupMembersTable.setCellPadding(0);
    pendingGroupMembersTable.setCellSpacing(0);

    int row = 0;

    FlexTable headerTable = new FlexTable();
    headerTable.setStyleName("baseTable");
    headerTable.setCellPadding(0);
    headerTable.setCellSpacing(0);

    final CheckBox selectAllCheckBox = new CheckBox();
    selectAllCheckBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        for (int row = 0; row < pendingGroupMembersTable.getRowCount(); row++) {
          ((CheckBox) pendingGroupMembersTable.getWidget(row, 0)).setValue(selectAllCheckBox.getValue());
          if (selectAllCheckBox.getValue()) {
            selectedPGMSet.add(tableRowToPGM.get(row));
          } else {
            selectedPGMSet.remove(tableRowToPGM.get(row));
          }
        }
      }
    });
    selectAllCheckBox.setTitle("Select All");
    headerTable.getCellFormatter().setStyleName(row, 0, "baseTableHeader");
    DOM.setStyleAttribute(selectAllCheckBox.getElement(), "paddingLeft", "5px");
    DOM.setStyleAttribute(selectAllCheckBox.getElement(), "paddingRight", "5px");

    headerTable.setWidget(row, 0, selectAllCheckBox);
    headerTable.setText(row, 1, "User");
    headerTable.getCellFormatter().setStyleName(row, 1, "baseTableHeader");
    DOM.setStyleAttribute(headerTable.getCellFormatter().getElement(row, 1), "paddingLeft", "5px");
    DOM.setStyleAttribute(headerTable.getCellFormatter().getElement(row, 1), "paddingRight", "5px");

    headerTable.setText(row, 2, "Group");
    headerTable.getCellFormatter().setStyleName(row, 2, "baseTableHeader");
    DOM.setStyleAttribute(headerTable.getCellFormatter().getElement(row, 2), "paddingLeft", "5px");
    DOM.setStyleAttribute(headerTable.getCellFormatter().getElement(row, 2), "paddingRight", "5px");

    headerTable.getCellFormatter().setWidth(0, 0, "30px");
    headerTable.getCellFormatter().setWidth(0, 1, "160px");
    headerTable.getCellFormatter().setWidth(0, 2, "160px");

    if (pendingList != null) {
      row = -1;
      for (final PendingGroupMembership membership : pendingList) {
        tableRowToPGM.put(++row, membership);

        final CheckBox check = new CheckBox();
        check.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
            selectedPGMSet.add(membership);
          }
        });
        DOM.setStyleAttribute(check.getElement(), "paddingLeft", "5px");
        DOM.setStyleAttribute(check.getElement(), "paddingRight", "5px");
        pendingGroupMembersTable.setWidget(row, 0, check);

        pendingGroupMembersTable.setText(row, 1, membership.getUser().getUsername());
        pendingGroupMembersTable.getCellFormatter().setStyleName(row, 1, "baseTable");
        DOM.setStyleAttribute(pendingGroupMembersTable.getCellFormatter().getElement(row, 1), "paddingLeft", "5px");
        DOM.setStyleAttribute(pendingGroupMembersTable.getCellFormatter().getElement(row, 1), "paddingRight", "5px");

        pendingGroupMembersTable.setText(row, 2, membership.getUserGroup().getName());
        pendingGroupMembersTable.getCellFormatter().setStyleName(row, 2, "baseTable");
        DOM.setStyleAttribute(pendingGroupMembersTable.getCellFormatter().getElement(row, 2), "paddingLeft", "5px");
        DOM.setStyleAttribute(pendingGroupMembersTable.getCellFormatter().getElement(row, 2), "paddingRight", "5px");

        pendingGroupMembersTable.getCellFormatter().setWidth(0, 0, "30px");
        pendingGroupMembersTable.getCellFormatter().setWidth(0, 1, "160px");
        pendingGroupMembersTable.getCellFormatter().setWidth(0, 2, "160px");
      }
    }

    // with selection, blah blah
    ScrollPanel headersScrollPanel = new ScrollPanel();
    headersScrollPanel.add(headerTable);

    ScrollPanel pendingMembersScrollPanel = new ScrollPanel();
    pendingMembersScrollPanel.add(pendingGroupMembersTable);
    pendingMembersScrollPanel.setHeight("200px");

    // create action panel (what to do)
    HorizontalPanel whatToDoPanel = new HorizontalPanel();
    whatToDoPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    whatToDoPanel.add(new Label("With selected: "));

    final ListBox choiceCombo = new ListBox(false);
    choiceCombo.addItem("Approve");
    choiceCombo.addItem("Deny");
    whatToDoPanel.add(choiceCombo);

    Button submitButton = new Button("Submit");
    submitButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        if (choiceCombo.getItemText(choiceCombo.getSelectedIndex()).equalsIgnoreCase("Approve")) {
          submitPendingGroupMembershipApproval(selectedPGMSet, true);
        } else {
          submitPendingGroupMembershipApproval(selectedPGMSet, false);
        }
      }
    });

    whatToDoPanel.add(submitButton);

    add(headersScrollPanel);
    add(pendingMembersScrollPanel);
    add(whatToDoPanel);

    setWidth("400px");

    if (parentPopup != null && parentPopup.isVisible()) {
      parentPopup.center();
    }
  }

  public void fetchPendingGroupMemberships() {
    final AsyncCallback<List<PendingGroupMembership>> approveCallback = new AsyncCallback<List<PendingGroupMembership>>() {
      public void onFailure(Throwable caught) {
        MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), true, true, true);
        dialog.center();
      }

      public void onSuccess(List<PendingGroupMembership> newPendingList) {
        populateUI(newPendingList);
      };
    };
    BaseServiceCache.getService().getPendingGroupMemberships(user, approveCallback);
  }

  public void submitPendingGroupMembershipApproval(Set<PendingGroupMembership> members, boolean approve) {
    // go to server and approve these group memberships
    final AsyncCallback<List<PendingGroupMembership>> approveCallback = new AsyncCallback<List<PendingGroupMembership>>() {
      public void onFailure(Throwable caught) {
        MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), true, true, true);
        dialog.center();
      }

      public void onSuccess(List<PendingGroupMembership> newPendingList) {
        populateUI(newPendingList);
      };
    };
    BaseServiceCache.getService().submitPendingGroupMembershipApproval(user, members, approve, approveCallback);
  }

}
