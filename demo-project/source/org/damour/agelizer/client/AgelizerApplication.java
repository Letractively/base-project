package org.damour.agelizer.client;

import java.util.Date;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.User;
import org.damour.base.client.service.BaseServiceAsync;
import org.damour.base.client.ui.TabWidget;
import org.damour.base.client.ui.admin.AdministratorPanel;
import org.damour.base.client.ui.admin.commands.CreateGroupCommand;
import org.damour.base.client.ui.admin.commands.JoinLeaveGroupsCommand;
import org.damour.base.client.ui.admin.commands.ManageMyGroupsCommand;
import org.damour.base.client.ui.admin.commands.ManagePendingGroupJoinsCommand;
import org.damour.base.client.ui.advisory.AdvisoryWidget;
import org.damour.base.client.ui.authentication.AuthenticationHandler;
import org.damour.base.client.ui.authentication.IAuthenticationListener;
import org.damour.base.client.ui.buttons.ComboMenuButton;
import org.damour.base.client.ui.buttons.MenuButtonCommand;
import org.damour.base.client.ui.buttons.ToolbarButton;
import org.damour.base.client.ui.comment.CommentWidget;
import org.damour.base.client.ui.rating.RatingWidget;
import org.damour.base.client.ui.repository.FileManagerPanel;
import org.damour.base.client.ui.toolbar.ToolBar;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AgelizerApplication implements EntryPoint, IAuthenticationListener {

  public static final String domain = "%domain%";

  FlexTable applicationPanel = new FlexTable();
  DeckPanel applicationContentDeck = new DeckPanel();
  ToolBar accountPanel = new ToolBar();
  FileManagerPanel fileManager = null;

  User user = null;

  public ComboMenuButton buildManageGroupsButton(boolean enabled) {
    MenuBar groupsMenu = new MenuBar(true);
    MenuItem manageGroupsMenuItem = new MenuItem("Manage My Groups", new ManageMyGroupsCommand(user));
    manageGroupsMenuItem.setTitle("Manage groups which you administer");
    groupsMenu.addItem(manageGroupsMenuItem);
    groupsMenu.addItem("Manage Group Requests", new ManagePendingGroupJoinsCommand(user));
    groupsMenu.addItem("Create New Group", new CreateGroupCommand(user));
    groupsMenu.addItem("Join/Leave Groups", new JoinLeaveGroupsCommand(user));

    ComboMenuButton menuButton = new ComboMenuButton("Groups", groupsMenu);
    menuButton.setEnabled(enabled);
    menuButton.setTitle("Create, manage or join groups");
    return menuButton;
  }

  public ComboMenuButton buildProfileButton(boolean enabled) {
    MenuBar profileMenu = new MenuBar(true);
    
    MenuItem editAccountMenuItem = new MenuItem("Account", new MenuButtonCommand() {
      public void execute() {
        popup.hide();
        // it is possible the user is 'stale', but HIGHLY unlikely
        AuthenticationHandler.getInstance().showEditAccountDialog(user);
      }
    });
    editAccountMenuItem.setTitle("Edit Your Account");
    profileMenu.addItem(editAccountMenuItem);

    MenuItem myFilesMenuItem = new MenuItem("File Manager", new MenuButtonCommand() {
      public void execute() {
        popup.hide();
        applicationContentDeck.showWidget(applicationContentDeck.getWidgetIndex(fileManager));
      }
    });
    myFilesMenuItem.setTitle("Manage Files");
    profileMenu.addItem(myFilesMenuItem);    
    
    ComboMenuButton menuButton = new ComboMenuButton("Profile", profileMenu);
    menuButton.setEnabled(enabled);
    menuButton.setTitle("Edit account, profile, photos and more");
    return menuButton;
  }  
  
  public void onModuleLoad() {
    AuthenticationHandler.getInstance().addLoginListener(this);

    applicationPanel.setWidth("100%");
    applicationPanel.setHeight("100%");
    applicationPanel.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
    applicationPanel.setWidget(0, 0, accountPanel);
    applicationPanel.getCellFormatter().setHeight(0, 0, "25px");

    applicationContentDeck.setHeight("100%");
    final ScrollPanel scroll = new ScrollPanel(applicationContentDeck);
    Window.addWindowResizeListener(new WindowResizeListener() {
      public void onWindowResized(int width, int height) {
        int newHeight = height - 120;
        if (newHeight >= 0) {
          scroll.setHeight(newHeight + "px");
          applicationPanel.getFlexCellFormatter().setHeight(1, 0, newHeight + "px");
        }
      }
    });
    scroll.setHeight((Window.getClientHeight() - 120) + "px");
    applicationPanel.getFlexCellFormatter().setHeight(1, 0, (Window.getClientHeight() - 120) + "px");
    applicationPanel.setWidget(1, 0, scroll);
    applicationPanel.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);

    applicationPanel.setWidget(2, 0, buildFooterPanel());
    applicationPanel.getFlexCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_BOTTOM);

    RootPanel.get("content").clear();
    RootPanel.get("content").add(applicationPanel);

    buildLoginUI();
    AuthenticationHandler.getInstance().setDomain(domain);
    AuthenticationHandler.getInstance().handleUserAuthentication(false);
  }

  public Widget buildFooterPanel() {
    VerticalPanel footerPanel = new VerticalPanel();
    footerPanel.setWidth("100%");

    FlexTable footerLinkPanel = new FlexTable();
    footerLinkPanel.setCellPadding(0);
    footerLinkPanel.setCellSpacing(5);
    footerLinkPanel.setStyleName("footerLinkPanel");

    Label advertiseLink = new Label("Advertise with Us", false);
    advertiseLink.setStyleName("footerLink");

    Label feedbackLink = new Label("Feedback", false);
    feedbackLink.setStyleName("footerLink");

    Label privacyLink = new Label("Privacy", false);
    privacyLink.setStyleName("footerLink");

    int linkCol = -1;
    footerLinkPanel.setWidth("100%");
    footerLinkPanel.setWidget(0, ++linkCol, new Label("", false));
    footerLinkPanel.getCellFormatter().setWidth(0, linkCol, "50%");
    footerLinkPanel.setWidget(0, ++linkCol, advertiseLink);
    footerLinkPanel.setWidget(0, ++linkCol, feedbackLink);
    footerLinkPanel.setWidget(0, ++linkCol, privacyLink);
    footerLinkPanel.setWidget(0, ++linkCol, new Label("", false));
    footerLinkPanel.getCellFormatter().setWidth(0, linkCol, "50%");

    footerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    footerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    footerPanel.add(footerLinkPanel);

    FlexTable footerBorderPanel = new FlexTable();
    footerBorderPanel.setCellPadding(0);
    footerBorderPanel.setCellSpacing(0);
    footerBorderPanel.setWidth("100%");
    footerBorderPanel.setStyleName("footerBorderPanel");
    footerBorderPanel.getCellFormatter().setHeight(0, 0, "12px");
    footerPanel.add(footerBorderPanel);

    VerticalPanel footerGradientPanelWrapper = new VerticalPanel();
    footerGradientPanelWrapper.setWidth("100%");
    footerGradientPanelWrapper.setStyleName("footerGradientPanel");

    final FlexTable footerGradientPanel = new FlexTable();
    footerGradientPanel.setCellPadding(0);
    footerGradientPanel.setCellSpacing(0);
    footerGradientPanel.setWidth("100%");

    linkCol = -1;
    int row = -1;
    footerGradientPanel.setHTML(++row, ++linkCol, "Copyright &#169 2007-" + ((new Date()).getYear() + 1900) + " " + domain + ".  All rights reserved.");
    footerGradientPanel.getCellFormatter().setHorizontalAlignment(row, linkCol, HasHorizontalAlignment.ALIGN_CENTER);

    linkCol = -1;
    final int dateRow = ++row;
    final int dateCol = ++linkCol;
    AsyncCallback<Date> serverStartupDateCallback = new AsyncCallback<Date>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(Date result) {
        footerGradientPanel.setHTML(dateRow, dateCol, "Server Up Since " + result.toLocaleString());
        footerGradientPanel.getCellFormatter().setHorizontalAlignment(dateRow, dateCol, HasHorizontalAlignment.ALIGN_CENTER);
      }
    };
    BaseServiceAsync.service.getServerStartupDate(serverStartupDateCallback);

    footerGradientPanelWrapper.add(footerGradientPanel);
    footerPanel.add(footerGradientPanelWrapper);

    return footerPanel;
  }

  public void buildLoginUI() {
    ToolbarButton loginLink = new ToolbarButton("Login");
    loginLink.setTitle("Login or Create a New Account");
    loginLink.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        AuthenticationHandler.getInstance().handleUserAuthentication(true);
      }
    });
    FlexTable loginPanelWrapper = new FlexTable();
    loginPanelWrapper.setWidth("100%");
    Label spacer = new Label();
    spacer.setWidth("100%");
    loginPanelWrapper.setWidget(0, 0, spacer);
    loginPanelWrapper.getCellFormatter().setWidth(0, 0, "100%");
    loginPanelWrapper.setWidget(0, 1, loginLink);
    loginPanelWrapper.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);

    accountPanel.clear();
    accountPanel.add(loginPanelWrapper);

    applicationContentDeck.clear();
    Launcher launcher = new Launcher(user);
    launcher.setHeight("100%");
    launcher.setWidth("100%");
    applicationContentDeck.add(launcher);
    applicationContentDeck.showWidget(applicationContentDeck.getWidgetIndex(launcher));
  }

  public void loadApplication() {
    applicationContentDeck.clear();
    final TabPanel tabs = new TabPanel();

    Label welcomeLabel = new Label("Welcome ", false);
    if (user.getFirstname() == null || "".equals(user.getFirstname())) {
      welcomeLabel.setText(welcomeLabel.getText() + user.getUsername());
    } else {
      welcomeLabel.setText(welcomeLabel.getText() + user.getFirstname());
    }
    welcomeLabel.setStyleName("welcomeLabel");

    ToolbarButton logoutLink = new ToolbarButton("Logout");
    logoutLink.setTitle("Sign out");
    logoutLink.addClickListener(new ClickListener() {
      public void onClick(final Widget sender) {
        AuthenticationHandler.getInstance().logout();
      }
    });

    FlexTable accountPanelWrapper = new FlexTable();
    int column = 0;
    accountPanelWrapper.setWidget(0, column, welcomeLabel);
    accountPanelWrapper.setWidget(0, ++column, buildProfileButton(true));
    accountPanelWrapper.setWidget(0, ++column, buildManageGroupsButton(true));
    accountPanelWrapper.setWidget(0, ++column, new Label());
    accountPanelWrapper.getCellFormatter().setWidth(0, column, "100%");
    if (user.isAdministrator()) {
      final ToolbarButton adminLink = new ToolbarButton("Administration");
      final AdministratorPanel adminPanel = new AdministratorPanel(user);
      applicationContentDeck.add(adminPanel);
      adminLink.addClickListener(new ClickListener() {
        public void onClick(final Widget sender) {
          adminPanel.activate();
          if (applicationContentDeck.getVisibleWidget() == applicationContentDeck.getWidgetIndex(adminPanel)) {
            applicationContentDeck.showWidget(applicationContentDeck.getWidgetIndex(tabs));
            adminLink.setText("Administration");
            adminLink.setTitle("Administration");
          } else {
            adminLink.setText("Return to Application");
            adminLink.setTitle("Return to Application");
            applicationContentDeck.showWidget(applicationContentDeck.getWidgetIndex(adminPanel));
          }
        }
      });
      accountPanelWrapper.setWidget(0, ++column, adminLink);
    }
    accountPanelWrapper.setWidget(0, ++column, logoutLink);
    accountPanelWrapper.getCellFormatter().setHorizontalAlignment(0, column, HasHorizontalAlignment.ALIGN_RIGHT);
    accountPanelWrapper.setWidth("100%");

    accountPanel.clear();
    accountPanel.setWidth("100%");
    accountPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    accountPanel.add(accountPanelWrapper);

    tabs.setAnimationEnabled(true);
    tabs.setWidth("100%");
    tabs.setHeight("100%");

    AsyncCallback<File> getFileCallback = new AsyncCallback<File>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(File file) {
        RatingWidget ratingWidget = new RatingWidget(file, null, true);
        tabs.add(ratingWidget, new TabWidget("Star Rating", false, tabs, ratingWidget));

        AdvisoryWidget advisoryWidget = new AdvisoryWidget(file, null, true);
        tabs.add(advisoryWidget, new TabWidget("Advisory Widget", false, tabs, advisoryWidget));

        CommentWidget commentWidget = new CommentWidget(file, null);
        tabs.add(commentWidget, new TabWidget("Comment Widget", false, tabs, commentWidget));
      }
    };
    BaseServiceAsync.service.getFile(1L, getFileCallback);

    fileManager = new FileManagerPanel("File Manager");
    fileManager.setHeight("100%");
    applicationContentDeck.add(fileManager);
    
    Launcher launcher = new Launcher(user);
    launcher.setHeight("100%");
    launcher.setWidth("100%");
    applicationContentDeck.add(launcher);
    applicationContentDeck.showWidget(applicationContentDeck.getWidgetIndex(launcher));

    applicationContentDeck.add(tabs);
    applicationContentDeck.setAnimationEnabled(true);
  }

  public void setAuthenticatedUser(User user) {
    this.user = user;
    if (user != null) {
      loadApplication();
    } else {
      buildLoginUI();
    }
  }

  public void loggedOut() {
    user = null;
    // fake the ui
    buildLoginUI();
  }

}
