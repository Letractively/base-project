package org.damour.base.server;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.damour.base.client.objects.Category;
import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.FileUploadStatus;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.HibernateStat;
import org.damour.base.client.objects.MemoryStats;
import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PendingGroupMembership;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.RepositoryTreeNode;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserAdvisory;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.objects.UserRating;
import org.damour.base.client.objects.Permission.PERM;
import org.damour.base.server.gwt.RemoteServiceServlet;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.ReflectionCache;
import org.damour.base.server.hibernate.helpers.AdvisoryHelper;
import org.damour.base.server.hibernate.helpers.DefaultData;
import org.damour.base.server.hibernate.helpers.CommentHelper;
import org.damour.base.server.hibernate.helpers.FileObjectHelper;
import org.damour.base.server.hibernate.helpers.FolderHelper;
import org.damour.base.server.hibernate.helpers.GenericPage;
import org.damour.base.server.hibernate.helpers.RatingHelper;
import org.damour.base.server.hibernate.helpers.RepositoryHelper;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.damour.base.server.hibernate.helpers.UserHelper;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;

import com.andrewtimberlake.captcha.Captcha;
import com.twmacinta.util.MD5;

public class BaseService extends RemoteServiceServlet implements org.damour.base.client.service.BaseService {

  public static HashMap<User, FileUploadStatus> fileUploadStatusMap = new HashMap<User, FileUploadStatus>();
  public static final int COOKIE_TIMEOUT = 31556926; // 1 year in seconds
  public static String smtpHost = "relay-hosting.secureserver.net";

  private Session session = null;

  static {
    bootstrap();
  }

  public static void bootstrap() {
    try {
      org.hibernate.Session session = HibernateUtil.getInstance().getSession();
      Transaction tx = session.beginTransaction();
      try {
        DefaultData.create(session);
        tx.commit();
      } catch (HibernateException he) {
        tx.rollback();
        session.close();
      } finally {
        try {
          session.close();
        } catch (Throwable t) {
        }
      }
    } catch (Throwable t) {
      BaseSystem.setDomainName("sometests.com");
      Logger.log(t);
    }
  }

  protected void onBeforeRequestDeserialized(String serializedRequest) {
    session = HibernateUtil.getInstance().getSession();
    Logger.log(serializedRequest);
  }

  protected void onAfterResponseSerialized(String serializedResponse) {
    try {
      session.close();
    } catch (Throwable t) {
    }
    Logger.log(serializedResponse);
  }

  protected void doUnexpectedFailure(Throwable e) {
    try {
      session.close();
    } catch (Throwable t) {
    }
    Logger.log(e);
    super.doUnexpectedFailure(e);
  }

  protected void sendDebugMessage(String text) {
    String from = "admin@" + getDomainName();
    String to = from;
    String subject = getDomainName() + " DEBUG";
    String message = "<BR/>" + text + "<BR/>";
    sendMessage(smtpHost, from, from, to, subject, message);
  }

  protected void sendMessage(String smtpHost, String fromAddress, String fromName, String to, String subject, String text) {
    try {
      // Get system properties
      Properties props = System.getProperties();
      // Setup mail server
      props.put("mail.smtp.host", smtpHost);
      // Get session
      javax.mail.Session session = javax.mail.Session.getDefaultInstance(props, null);
      // Define message
      MimeMessage message = new MimeMessage(session);
      // Set the from address
      message.setFrom(new InternetAddress(fromAddress, fromName));
      // Set the to address
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      // Set the subject
      message.setSubject(subject);
      // Set the content
      message.setContent(text, "text/html");
      // Send message
      Transport.send(message);
    } catch (Exception e) {
    }
  }

  protected String getSmtpHost() {
    return smtpHost;
  }

  protected void setSmtpHost(String inSmtpHost) {
    smtpHost = inSmtpHost;
  }

  protected String getDomainName() {
    return BaseSystem.getDomainName(getThreadLocalRequest());
  }

//  protected void emailException(Throwable t) {
//    String trace = Logger.convertThrowableToHTML(t);
//    String from = "admin@" + getDomainName();
//    String to = from;
//    String subject = "A critical server error has occurred.";
//    String message = "<BR/>" + t.getMessage() + "<BR/>" + trace;
//    sendMessage(smtpHost, from, from, to, subject, message);
//  }

//  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//    String method = req.getParameter("method");
//    if (method.equalsIgnoreCase("doesUserHavePermission")) {
//      org.hibernate.Session session = HibernateUtil.getInstance().getSession();
//      try {
//        String username = req.getParameter("user");
//        String permissionStr = req.getParameter("permission");
//        Long permissibleObjectId = Long.valueOf(req.getParameter("permissibleObjectId"));
//        User user = UserHelper.getUser(session, username);
//        PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, permissibleObjectId);
//        PERM permission = PERM.valueOf(permissionStr);
//        resp.setContentType("text/xml");
//        resp.getWriter().write("<result>");
//        resp.getWriter().write("" + SecurityHelper.doesUserHavePermission(session, user, permissibleObject, permission));
//        resp.getWriter().write("</result>");
//      } catch (Throwable t) {
//        t.printStackTrace();
//        logException(t);
//        throw new RuntimeException("Invalid doGet request");
//      } finally {
//        session.close();
//      }
//    } else if (method.equals("evictFile")) {
//      org.hibernate.Session session = HibernateUtil.getInstance().getSession();
//      try {
//        Long id = Long.valueOf(req.getParameter("id"));
//        File file = (File) session.load(File.class, id);
//        session.evict(file);
//        session.getSessionFactory().evict(File.class, id);
//      } catch (Throwable t) {
//        logException(t);
//        throw new RuntimeException("Invalid doGet request");
//      } finally {
//        session.close();
//      }
//    }
//  }

  public User login(HttpServletRequest request, HttpServletResponse response, String username, String password) {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      return login(session, request, response, username, password, false);
    } catch (Throwable t) {
      Logger.log(t);
      throw new RuntimeException("Could not login.  Invalid username or password.");
    } finally {
      session.close();
    }
  }

  public User login(String username, String password) throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      return login(session, getThreadLocalRequest(), getThreadLocalResponse(), username, password, false);
    } catch (Throwable t) {
      Logger.log(t);
      throw new RuntimeException("Could not login.  Invalid username or password.");
    } finally {
      session.close();
    }
  }

  private User login(org.hibernate.Session session, HttpServletRequest request, HttpServletResponse response, String username, String password, boolean internal) throws Exception {
    username = username.toLowerCase();
    User user = UserHelper.getUser(session, username);
    MD5 md5 = new MD5();
    md5.Update(password);
    String passwordHash = md5.asHex();
    if (user != null && user.getUsername().equals(username) && ((internal && password.equals(user.getPasswordHash())) || user.getPasswordHash().equals(passwordHash))) {
      Cookie userCookie = new Cookie("user", user.getUsername());
      userCookie.setPath("/");
      userCookie.setMaxAge(COOKIE_TIMEOUT);
      Cookie userAuthCookie = new Cookie("auth", internal ? password : passwordHash);
      userAuthCookie.setPath("/");
      userAuthCookie.setMaxAge(COOKIE_TIMEOUT);
      response.addCookie(userCookie);
      response.addCookie(userAuthCookie);
    } else {
      destroyCookies(response);
      throw new RuntimeException("Could not login.  Invalid username or password.");
    }
    return user;
  }

  private void destroyCookies(HttpServletResponse response) throws Exception {
    Cookie userCookie = new Cookie("user", "");
    userCookie.setMaxAge(0);
    userCookie.setPath("/");
    Cookie userAuthCookie = new Cookie("auth", "");
    userAuthCookie.setMaxAge(0);
    userAuthCookie.setPath("/");
    response.addCookie(userCookie);
    response.addCookie(userAuthCookie);
  }

  public User getAuthenticatedUser(org.hibernate.Session session, HttpServletRequest request, HttpServletResponse response) {
    Cookie cookies[] = request.getCookies();
    Cookie userCookie = null;
    Cookie userAuthCookie = null;
    for (int i = 0; cookies != null && i < cookies.length; i++) {
      if (cookies[i].getName().equals("user") && !cookies[i].getValue().equals("")) {
        userCookie = cookies[i];
      } else if (cookies[i].getName().equals("auth") && !cookies[i].getValue().equals("")) {
        userAuthCookie = cookies[i];
      }
    }
    if (userCookie == null || userAuthCookie == null) {
      return null;
    }
    String username = userCookie.getValue().toLowerCase();
    User user = UserHelper.getUser(session, username);
    if (user != null && userAuthCookie.getValue().equals(user.getPasswordHash())) {
      return user;
    }
    return null;
  }

  protected User getAuthenticatedUser(org.hibernate.Session session) throws Exception {
    return getAuthenticatedUser(session, getThreadLocalRequest(), getThreadLocalResponse());
  }

  public User getAuthenticatedUser() throws Exception {
    Cookie cookies[] = getThreadLocalRequest().getCookies();
    Cookie userCookie = null;
    Cookie userAuthCookie = null;
    for (int i = 0; cookies != null && i < cookies.length; i++) {
      if (cookies[i].getName().equals("user") && !cookies[i].getValue().equals("")) {
        userCookie = cookies[i];
      } else if (cookies[i].getName().equals("auth") && !cookies[i].getValue().equals("")) {
        userAuthCookie = cookies[i];
      }
    }
    if (userCookie == null || userAuthCookie == null) {
      throw new RuntimeException("Could not get authenticated user.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      User user = getAuthenticatedUser(session);
      if (user == null) {
        destroyCookies(getThreadLocalResponse());
        throw new RuntimeException("Could not get authenticated user.");
      }
      return user;
    } finally {
      session.close();
    }
  }

  public void logout() throws Exception {
    destroyCookies(getThreadLocalResponse());
  }

  // create or edit account
  public User createOrEditAccount(User inUser, String password, String captchaText) throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();
    try {
      User possibleAuthUser = getAuthenticatedUser(session);
      User authUser = null;
      if (possibleAuthUser instanceof User) {
        authUser = (User) possibleAuthUser;
      }

      User dbUser = null;
      try {
        dbUser = (User) session.load(User.class, inUser.getId());
      } catch (Exception e) {
      }

      if (dbUser == null) {
        // new account, it did NOT exist
        // validate captcha first
        if (captchaText != null && !"".equals(captchaText)) {
          Captcha captcha = (Captcha)getThreadLocalRequest().getSession().getAttribute("captcha");
          if (!captcha.isValid(captchaText)) {
            throw new RuntimeException("Could not create account: validation failed.");
          }
        }

        User newUser = new User();
        newUser.setUsername(inUser.getUsername().toLowerCase());
        if (password != null && !"".equals(password)) {
          MD5 md5 = new MD5();
          md5.Update(password);
          newUser.setPasswordHash(md5.asHex());
        }
        if (authUser != null && authUser.isAdministrator()) {
          newUser.setAdministrator(inUser.isAdministrator());
        }
        newUser.setFirstname(inUser.getFirstname());
        newUser.setLastname(inUser.getLastname());
        newUser.setEmail(inUser.getEmail());
        newUser.setBirthday(inUser.getBirthday());
        newUser.setPasswordHint(inUser.getPasswordHint());
        newUser.setValidated(inUser.isValidated());

        session.save(newUser);

        UserGroup userGroup = new UserGroup();
        userGroup.setName(newUser.getUsername());
        userGroup.setOwner(newUser);

        session.save(userGroup);

        tx.commit();

        // if we are a true new unauthenticated user, create a new account
        if (authUser == null) {
          destroyCookies(getThreadLocalResponse());
          if (login(session, getThreadLocalRequest(), getThreadLocalResponse(), newUser.getUsername(), newUser.getPasswordHash(), true) != null) {
            return newUser;
          }
        }
        return newUser;
      } else if (authUser != null && (authUser.isAdministrator() || authUser.getId().equals(dbUser.getId()))) {
        // edit an existing account
        // the following conditions must be met to be here:
        // -authentication
        // -we are the administrator
        // -we are editing our own account
        if (password != null && !"".equals(password)) {
          MD5 md5 = new MD5();
          md5.Update(password);
          dbUser.setPasswordHash(md5.asHex());
        }
        if (authUser.isAdministrator()) {
          dbUser.setAdministrator(inUser.isAdministrator());
        }
        dbUser.setUsername(inUser.getUsername());
        dbUser.setFirstname(inUser.getFirstname());
        dbUser.setLastname(inUser.getLastname());
        dbUser.setEmail(inUser.getEmail());
        dbUser.setBirthday(inUser.getBirthday());
        dbUser.setPasswordHint(inUser.getPasswordHint());
        dbUser.setValidated(inUser.isValidated());

        session.save(dbUser);
        tx.commit();

        // if we are editing our own account, then re-authenticate
        if (authUser.getId().equals(dbUser.getId())) {
          destroyCookies(getThreadLocalResponse());
          if (login(session, getThreadLocalRequest(), getThreadLocalResponse(), dbUser.getUsername(), dbUser.getPasswordHash(), true) != null) {
            return dbUser;
          }
        }
        return dbUser;
      }
      throw new RuntimeException("Could not edit account.");
    } catch (Exception ex) {
      ex.printStackTrace();
      try {
        tx.rollback();
      } catch (Exception exx) {
      }
      throw new RuntimeException(ex.getMessage());
    } finally {
      session.close();
    }
  }

  public String getLoginHint(String username) throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      User user = UserHelper.getUser(session, username.toLowerCase());
      if (user == null) {
        throw new RuntimeException("Could not get login hint.");
      }
      return user.getPasswordHint();
    } finally {
      session.close();
    }
  }

  public List<String> getUsernames() throws Exception {
    // this is a non-admin function
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      return SecurityHelper.getUsernames(session);
    } finally {
      session.close();
    }
  }

  public List<User> getUsers() throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      return SecurityHelper.getUsers(session);
    } finally {
      session.close();
    }
  }

  public List<UserGroup> getGroups(User user) throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      User authUser = getAuthenticatedUser(session);
      // the admin & actual user can list all groups for the user
      if (authUser != null && (authUser.isAdministrator() || authUser.equals(user))) {
        return SecurityHelper.getUserGroups(session, user);
      }
      // everyone else can only see visible groups for the user
      return SecurityHelper.getVisibleUserGroups(session, user);

    } finally {
      session.close();
    }
  }

  public List<UserGroup> getOwnedGroups(User user) throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      User authUser = getAuthenticatedUser(session);
      // if we are the admin, and we are asking to list owned admin groups,
      // we show all groups to the admin
      if (authUser != null && authUser.isAdministrator()) {
        return SecurityHelper.getUserGroups(session);
      }
      // the actual user can list all owned groups for the user
      if (authUser != null && authUser.equals(user)) {
        return SecurityHelper.getOwnedUserGroups(session, user);
      }
      // if we are not the admin or the actual user, we can only list the visible groups
      // to unknown people
      return SecurityHelper.getOwnedVisibleUserGroups(session, user);
    } finally {
      session.close();
    }
  }

  public List<UserGroup> getGroups() throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      User authUser = getAuthenticatedUser(session);
      // the admin can list all groups
      if (authUser != null && authUser.isAdministrator()) {
        return SecurityHelper.getUserGroups(session);
      }
      return SecurityHelper.getVisibleUserGroups(session);
    } finally {
      session.close();
    }
  }

  public List<User> getUsers(UserGroup group) throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      User authUser = getAuthenticatedUser(session);
      if (authUser == null) {
        throw new RuntimeException("User is not authenticated.");
      }
      group = (UserGroup) session.load(UserGroup.class, group.getId());
      // only the group owner, group members and administrator can see the users in a group
      if (authUser.isAdministrator() || authUser.equals(group.getOwner())) {
        return SecurityHelper.getUsersInUserGroup(session, group);
      }
      // now check the groups for the user against the group
      List<GroupMembership> memberships = SecurityHelper.getGroupMemberships(session, authUser);
      if (memberships.contains(group)) {
        return SecurityHelper.getUsersInUserGroup(session, group);
      }
      throw new RuntimeException("User is not authorized to list users in group.");
    } finally {
      session.close();
    }
  }

  public GroupMembership addUserToGroup(User user, UserGroup group) throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = null;
    try {
      User authUser = getAuthenticatedUser(session);
      if (authUser == null) {
        throw new RuntimeException("Could not join group, attempt to join with unauthorized client.");
      }
      group = (UserGroup) session.load(UserGroup.class, group.getId());
      user = (User) session.load(User.class, user.getId());

      if (group == null || user == null) {
        throw new RuntimeException("Could not join group, user and group not found.");
      }

      // the group owner and an administrator may add users to groups without obeying the 'lock'
      if (group.isLocked() && !authUser.isAdministrator() && !group.getOwner().getId().equals(authUser.getId())) {
        throw new RuntimeException("This group is currently not accepting new members.");
      }

      if (authUser.isAdministrator() || group.isAutoJoin() || group.getOwner().getId().equals(authUser.getId())) {
        tx = session.beginTransaction();
        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setUser(user);
        groupMembership.setUserGroup(group);
        session.save(groupMembership);
        tx.commit();
        return groupMembership;
      } else if (!group.isAutoJoin()) {
        tx = session.beginTransaction();
        PendingGroupMembership groupMembership = new PendingGroupMembership();
        groupMembership.setUser(user);
        groupMembership.setUserGroup(group);
        session.save(groupMembership);
        tx.commit();
        // send email to group owner
        sendMessage(getSmtpHost(), "admin@" + getDomainName(), "admin@" + getDomainName(), group.getOwner().getEmail(), "Group join request from " + user.getUsername(), "[" + getDomainName() + "] " + user.getUsername()
            + " has requested permission to join your group " + group.getName());
        throw new RuntimeException("Could not join group, request submitted to group owner.");
      }
      throw new RuntimeException("Could not join group.");
    } catch (org.hibernate.exception.ConstraintViolationException e) {
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException("Could not join group, user already a member or add request pending.");
    } finally {
      session.close();
    }
  }

  public List<PendingGroupMembership> getPendingGroupMemberships(User user) throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      User authUser = getAuthenticatedUser(session);
      if (authUser == null) {
        throw new RuntimeException("Could not join group, attempt to join with unauthorized client.");
      }
      user = (User) session.load(User.class, user.getId());

      if (user == null) {
        throw new RuntimeException("Could not get pending groups for supplied user.");
      }

      if (authUser.isAdministrator() || user.getId().equals(authUser.getId())) {
        // remember, administrator owns all
        return SecurityHelper.getPendingGroupMemberships(session, user);
      } else {
        throw new RuntimeException("Could not get pending group memberships.");
      }

    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public List<PendingGroupMembership> submitPendingGroupMembershipApproval(User user, Set<PendingGroupMembership> members, boolean approve) throws Exception {

    if (members == null || members.size() == 0) {
      throw new RuntimeException("List of members provided was empty.");
    }

    if (user == null) {
      throw new RuntimeException("User not supplied.");
    }

    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();
    try {
      User authUser = getAuthenticatedUser(session);
      if (authUser == null) {
        throw new RuntimeException("Cannot approve or deny requests without authentication.");
      }

      // only the authenticated: admin or user themselves
      if (authUser.isAdministrator() || user.getId().equals(authUser.getId())) {
        for (PendingGroupMembership pendingGroupMembership : members) {
          // if we are the admin or to be sure that the user actually owns the group for this pending request
          if (authUser.isAdministrator() || user.getId().equals(pendingGroupMembership.getUserGroup().getOwner().getId())) {
            // approve/deny request
            if (approve) {
              GroupMembership realGroupMembership = new GroupMembership();
              realGroupMembership.setUser(pendingGroupMembership.getUser());
              realGroupMembership.setUserGroup(pendingGroupMembership.getUserGroup());
              session.save(realGroupMembership);
            }
            session.delete(pendingGroupMembership);
          }
        }
        tx.commit();
        // send back the new list
        return SecurityHelper.getPendingGroupMemberships(session, user);
      } else {
        throw new RuntimeException("Cannot approve or deny requests without proper authentication.");
      }
    } catch (Throwable t) {
      t.printStackTrace();
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public UserGroup createOrEditGroup(UserGroup group) throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();
    try {
      User authUser = getAuthenticatedUser(session);
      if (authUser != null && (authUser.isAdministrator() || authUser.getId().equals(group.getOwner().getId()))) {
        try {
          User owner = (User) session.load(User.class, group.getOwner().getId());
          group.setOwner(owner);
        } catch (HibernateException e) {
        }

        if (group.getId() == null) {
          // new group
          // before we save, let's make sure the user doesn't already have a group by this name
          List<UserGroup> existingGroups = SecurityHelper.getOwnedUserGroups(session, group.getOwner());
          for (UserGroup existingGroup : existingGroups) {
            if (existingGroup.getName().equalsIgnoreCase(group.getName())) {
              throw new RuntimeException("A group already exists with this name.");
            }
          }
          session.save(group);
          // default is to create membership for the owner
          GroupMembership groupMembership = new GroupMembership();
          groupMembership.setUser(group.getOwner());
          groupMembership.setUserGroup(group);
          session.save(groupMembership);
        } else {
          // let's make sure that if we are changing the group name that
          // the only group with this name (for the group owner) is this group
          session.saveOrUpdate(group);
        }

        tx.commit();
        return group;
      }
      return null;
    } catch (Throwable t) {
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public void deleteUser(User user, UserGroup group) throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      User authUser = getAuthenticatedUser(session);
      if (authUser == null) {
        throw new RuntimeException("Could not remove user from group, attempt made with unauthorized client.");
      }
      group = (UserGroup) session.load(UserGroup.class, group.getId());
      user = (User) session.load(User.class, user.getId());

      if (group == null || user == null) {
        throw new RuntimeException("Could not remove user from group, user or group not found.");
      }

      if (authUser.isAdministrator() || group.isAutoJoin() || group.getOwner().getId().equals(authUser.getId())) {
        Transaction tx = session.beginTransaction();
        GroupMembership groupMembership = SecurityHelper.getGroupMembership(session, user, group);
        if (groupMembership != null) {
          session.delete(groupMembership);
        }
        tx.commit();
      }
    } finally {
      session.close();
    }
  }

  public void deleteGroup(UserGroup group) throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      User authUser = getAuthenticatedUser(session);
      if (authUser != null && (authUser.isAdministrator() || group.getOwner().getId().equals(authUser.getId()))) {
        Transaction tx = session.beginTransaction();
        group = (UserGroup) session.load(UserGroup.class, group.getId());
        SecurityHelper.deleteUserGroup(session, group);
        tx.commit();
      } else {
        throw new RuntimeException("Could not delete group, insufficient privilidges.");
      }
    } finally {
      session.close();
    }
  }

  public List<HibernateStat> getHibernateStats() throws Exception {
    List<HibernateStat> statsList = new ArrayList<HibernateStat>();

    Statistics stats = HibernateUtil.getInstance().getSessionFactory().getStatistics();

    String regionNames[] = stats.getSecondLevelCacheRegionNames();
    for (String regionName : regionNames) {
      SecondLevelCacheStatistics regionStat = stats.getSecondLevelCacheStatistics(regionName);

      HibernateStat newstat = new HibernateStat();
      newstat.setRegionName(regionName);
      newstat.setCachePuts(regionStat.getPutCount());
      newstat.setCacheHits(regionStat.getHitCount());
      newstat.setCacheMisses(regionStat.getMissCount());
      newstat.setMemoryUsed(regionStat.getSizeInMemory());
      newstat.setNumObjectsInCache(regionStat.getElementCountInMemory());
      newstat.setNumObjectsOnDisk(regionStat.getElementCountOnDisk());
      statsList.add(newstat);
    }

    return statsList;
  }

  public void resetHibernate() throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      User authUser = getAuthenticatedUser(session);
      if (authUser != null && authUser.isAdministrator()) {
        HibernateUtil.getInstance().resetHibernate();
      }
    } finally {
      try {
        session.close();
      } catch (Throwable t) {
      }
    }
  }

  public void evictClassFromCache(String className) throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      User authUser = getAuthenticatedUser(session);
      if (authUser != null && authUser.isAdministrator()) {
        try {
          Class clazz = Class.forName(className);
          HibernateUtil.getInstance().getSessionFactory().evict(clazz);
          Logger.log("Evicted: " + className);
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    } finally {
      try {
        session.close();
      } catch (Throwable t) {
      }
    }
  }

  public MemoryStats getMemoryStats() throws Exception {
    MemoryStats stats = new MemoryStats();
    stats.setMaxMemory(Runtime.getRuntime().maxMemory());
    stats.setTotalMemory(Runtime.getRuntime().totalMemory());
    stats.setFreeMemory(Runtime.getRuntime().freeMemory());
    return stats;
  }

  public MemoryStats requestGarbageCollection() throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    try {
      User authUser = getAuthenticatedUser(session);
      if (authUser != null && authUser.isAdministrator()) {
        try {
          System.gc();
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    } finally {
      try {
        session.close();
      } catch (Throwable t) {
      }
    }
    return getMemoryStats();
  }

  public Date getServerStartupDate() throws Exception {
    return new Date(HibernateUtil.getInstance().getStartupDate());
  }

  public UserRating getUserRating(PermissibleObject permissibleObject) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("PermissibleObject not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    try {
      permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, permissibleObject.getId());
      if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
        throw new RuntimeException("User is not authorized to get rating on this content.");
      }
      // find rating based on remote address if needed
      return RatingHelper.getUserRating(session, permissibleObject, authUser, getThreadLocalRequest().getRemoteAddr());
    } catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public UserRating setUserRating(PermissibleObject permissibleObject, int rating) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("PermissibleObject not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    Transaction tx = session.beginTransaction();
    try {
      permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, permissibleObject.getId());

      if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
        throw new RuntimeException("User is not authorized to set rating on this content.");
      }

      float totalRating = (float) permissibleObject.getNumRatingVotes() * permissibleObject.getAverageRating();
      totalRating += rating;
      permissibleObject.setNumRatingVotes(permissibleObject.getNumRatingVotes() + 1);
      float newAvg = totalRating / (float) permissibleObject.getNumRatingVotes();
      permissibleObject.setAverageRating(newAvg);
      session.save(permissibleObject);

      UserRating userRating = RatingHelper.getUserRating(session, permissibleObject, authUser, getThreadLocalRequest().getRemoteAddr());

      if (userRating != null) {
        throw new RuntimeException("Already voted.");
      }

      // check if rating already exists
      userRating = new UserRating();
      userRating.setPermissibleObject(permissibleObject);
      userRating.setRating(rating);
      userRating.setVoter(authUser);
      userRating.setVoterIP(getThreadLocalRequest().getRemoteAddr());

      session.save(userRating);
      tx.commit();
      return userRating;
    } catch (Throwable t) {
      t.printStackTrace();
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public UserAdvisory getUserAdvisory(PermissibleObject permissibleObject) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("PermissibleObject not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    try {
      permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, permissibleObject.getId());

      if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
        throw new RuntimeException("User is not authorized to get advisory on this content.");
      }
      // find rating based on remote address if needed
      return AdvisoryHelper.getUserAdvisory(session, permissibleObject, authUser, getThreadLocalRequest().getRemoteAddr());
    } catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public UserAdvisory setUserAdvisory(PermissibleObject permissibleObject, int advisory) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("PermissibleObject not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    Transaction tx = session.beginTransaction();
    try {
      permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, permissibleObject.getId());

      if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
        throw new RuntimeException("User is not authorized to set advisory on this content.");
      }

      float totalAdvisory = (float) permissibleObject.getNumAdvisoryVotes() * permissibleObject.getAverageAdvisory();
      totalAdvisory += advisory;
      permissibleObject.setNumAdvisoryVotes(permissibleObject.getNumAdvisoryVotes() + 1);
      float newAvg = totalAdvisory / (float) permissibleObject.getNumAdvisoryVotes();
      permissibleObject.setAverageAdvisory(newAvg);
      session.save(permissibleObject);

      UserAdvisory userAdvisory = AdvisoryHelper.getUserAdvisory(session, permissibleObject, authUser, getThreadLocalRequest().getRemoteAddr());

      if (userAdvisory != null) {
        throw new RuntimeException("Already voted.");
      }

      // check if rating already exists
      userAdvisory = new UserAdvisory();
      userAdvisory.setPermissibleObject(permissibleObject);
      userAdvisory.setRating(advisory);
      userAdvisory.setVoter(authUser);
      userAdvisory.setVoterIP(getThreadLocalRequest().getRemoteAddr());

      session.save(userAdvisory);
      tx.commit();
      return userAdvisory;
    } catch (Throwable t) {
      t.printStackTrace();
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public List<Comment> getComments(PermissibleObject permissibleObject) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("File not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    try {
      permissibleObject = ((PermissibleObject) session.load(File.class, permissibleObject.getId()));
      if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
        throw new RuntimeException("User is not authorized to get comments on this content.");
      }
      return CommentHelper.getComments(session, permissibleObject);
    } catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public Page<Comment> getCommentPage(PermissibleObject permissibleObject, boolean sortDescending, int pageNumber, int pageSize) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("File not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    try {
      permissibleObject = ((PermissibleObject) session.load(File.class, permissibleObject.getId()));
      if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
        throw new RuntimeException("User is not authorized to get comments on this content.");
      }
      GenericPage<Comment> gPage = new GenericPage<Comment>(session, "from " + Comment.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.getId() + " order by id " + (sortDescending ? "desc" : "asc"), pageNumber, pageSize);
      return new Page<Comment>(gPage.getList(), pageNumber, gPage.getLastPageNumber(), gPage.getRowCount());
    } catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public Boolean submitComment(Comment comment) throws Exception {
    if (comment == null) {
      throw new RuntimeException("Comment not supplied.");
    }
    if (comment.getPermissibleObject() == null) {
      throw new RuntimeException("PermissibleObject not supplied with comment.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    Transaction tx = session.beginTransaction();
    try {
      comment.setPermissibleObject((PermissibleObject) session.load(PermissibleObject.class, comment.getPermissibleObject().getId()));
      if (!SecurityHelper.doesUserHavePermission(session, authUser, comment.getPermissibleObject(), PERM.READ)) {
        throw new RuntimeException("User is not authorized to make comments on this content.");
      }
      if (!comment.permissibleObject.isAllowComments()) {
        throw new RuntimeException("Comments are not allowed on this content.");
      }
      // the comment is approved if we are not moderating or if the commenter is the file owner
      comment.setApproved(!comment.permissibleObject.isModerateComments() || comment.getPermissibleObject().getOwner().equals(authUser));
      session.save(comment);
      tx.commit();
      return true;
    } catch (Throwable t) {
      t.printStackTrace();
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public Boolean approveComment(Comment comment) throws Exception {
    if (comment == null) {
      throw new RuntimeException("Comment not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    if (authUser == null) {
      throw new RuntimeException(".");
    }
    Transaction tx = session.beginTransaction();
    try {
      comment = ((Comment) session.load(Comment.class, comment.getId()));
      if (!SecurityHelper.doesUserHavePermission(session, authUser, comment.getPermissibleObject(), PERM.WRITE)) {
        throw new RuntimeException("User is not authorized to approve comments for this content.");
      }
      comment.setApproved(true);
      session.save(comment);
      tx.commit();
      return true;
    } catch (Throwable t) {
      t.printStackTrace();
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public Boolean deleteComment(Comment comment) throws Exception {
    if (comment == null) {
      throw new RuntimeException("Comment not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.beginTransaction();
    try {
      comment = ((Comment) session.load(Comment.class, comment.getId()));
      if (!comment.getAuthor().equals(authUser) && !SecurityHelper.doesUserHavePermission(session, authUser, comment.getPermissibleObject(), PERM.WRITE)) {
        throw new RuntimeException("User is not authorized to delete comments for this content.");
      }
      // we can't delete this comment until we delete all the child comments
      CommentHelper.deleteComment(session, comment);
      tx.commit();
      return true;
    } catch (Throwable t) {
      t.printStackTrace();
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public File getFile(Long id) throws Exception {
    if (id == null) {
      throw new RuntimeException("Id not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    try {
      File file = (File) session.load(File.class, id);
      if (!SecurityHelper.doesUserHavePermission(session, authUser, file, PERM.READ)) {
        throw new RuntimeException("User is not authorized to get this content.");
      }
      return file;
    } catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public RepositoryTreeNode getRepositoryTree() throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    try {
      RepositoryTreeNode root = new RepositoryTreeNode();
      RepositoryHelper.buildRepositoryTreeNode(session, authUser, root, null);
      return root;
    } catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public Folder createNewFolder(Folder newFolder) throws Exception {
    if (newFolder == null) {
      throw new RuntimeException("Folder not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.beginTransaction();
    try {
      if (newFolder.getParent() != null) {
        newFolder.setParent((PermissibleObject) session.load(PermissibleObject.class, newFolder.getParent().getId()));
      }
      if (!SecurityHelper.doesUserHavePermission(session, authUser, newFolder.getParent(), PERM.WRITE)) {
        throw new RuntimeException("User is not authorized to create a new folder here.");
      }
      if (newFolder.getId() != null) {
        Folder hibNewFolder = (Folder) session.load(Folder.class, newFolder.getId());
        if (hibNewFolder != null) {
          if (!SecurityHelper.doesUserHavePermission(session, authUser, hibNewFolder, PERM.WRITE)) {
            throw new RuntimeException("User is not authorized to save a new folder here.");
          }
          hibNewFolder.setName(newFolder.getName());
          hibNewFolder.setDescription(newFolder.getDescription());
          hibNewFolder.setParent(newFolder.getParent());
          newFolder = hibNewFolder;
        }
      }

      newFolder.setOwner(authUser);
      session.save(newFolder);
      tx.commit();
      return newFolder;
    } catch (Throwable t) {
      t.printStackTrace();
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public void deleteFile(File file) throws Exception {
    if (file == null) {
      throw new RuntimeException("File not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.beginTransaction();
    try {
      file = (File) session.load(File.class, file.getId());
      if (!SecurityHelper.doesUserHavePermission(session, authUser, file, PERM.WRITE)) {
        throw new RuntimeException("User is not authorized to delete this file.");
      }
      FileObjectHelper.deleteFile(session, file);
      tx.commit();
    } catch (Throwable t) {
      t.printStackTrace();
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public void deleteFolder(Folder folder) throws Exception {
    if (folder == null) {
      throw new RuntimeException("Folder not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.beginTransaction();
    try {
      folder = (Folder) session.load(Folder.class, folder.getId());
      if (!authUser.isAdministrator() && !authUser.equals(folder.getOwner())) {
        throw new RuntimeException("User is not authorized to delete this folder.");
      }
      FolderHelper.deleteFolder(session, folder);
      tx.commit();
    } catch (Throwable t) {
      t.printStackTrace();
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public void renameFile(File file) throws Exception {
    if (file == null) {
      throw new RuntimeException("File not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.beginTransaction();
    try {
      File hibfile = (File) session.load(File.class, file.getId());
      if (!SecurityHelper.doesUserHavePermission(session, authUser, hibfile, PERM.WRITE)) {
        throw new RuntimeException("User is not authorized to rename this file.");
      }
      hibfile.setName(file.getName());
      session.save(hibfile);
      tx.commit();
    } catch (Throwable t) {
      t.printStackTrace();
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public void renameFolder(Folder folder) throws Exception {
    if (folder == null) {
      throw new RuntimeException("Folder not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.beginTransaction();
    try {
      Folder hibfolder = (Folder) session.load(Folder.class, folder.getId());
      if (!SecurityHelper.doesUserHavePermission(session, authUser, hibfolder, PERM.WRITE)) {
        throw new RuntimeException("User is not authorized to rename this folder.");
      }
      hibfolder.setName(folder.getName());
      session.save(hibfolder);
      tx.commit();
    } catch (Throwable t) {
      t.printStackTrace();
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public List<Permission> getPermissions(PermissibleObject permissibleObject) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("PermissibleObject not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    try {
      permissibleObject = ((PermissibleObject) session.load(PermissibleObject.class, permissibleObject.getId()));
      if (!authUser.isAdministrator() && !permissibleObject.getOwner().equals(authUser)) {
        throw new RuntimeException("User is not authorized to get permissions on this content.");
      }
      return SecurityHelper.getPermissions(session, permissibleObject);
    } catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public PermissibleObject updatePermissibleObject(PermissibleObject permissibleObject) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("PermissibleObject not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.beginTransaction();
    try {
      User newOwner = ((User) session.load(User.class, permissibleObject.getOwner().getId()));
      PermissibleObject hibPermissibleObject = ((PermissibleObject) session.load(PermissibleObject.class, permissibleObject.getId()));
      if (!authUser.isAdministrator() && !hibPermissibleObject.getOwner().equals(authUser)) {
        throw new RuntimeException("User is not authorized to update this object.");
      }
      // otherwise hibernate will be upset:
      hibPermissibleObject.setName(permissibleObject.getName());
      hibPermissibleObject.setOwner(newOwner);
      hibPermissibleObject.setGlobalRead(permissibleObject.isGlobalRead());
      hibPermissibleObject.setGlobalWrite(permissibleObject.isGlobalWrite());
      hibPermissibleObject.setGlobalExecute(permissibleObject.isGlobalExecute());

      // update 'child' fields
      Field fields[] = ReflectionCache.getFields(hibPermissibleObject.getClass());
      for (Field field : fields) {
        try {
          Object obj = field.get(hibPermissibleObject);
          if (obj instanceof PermissibleObject) {
            PermissibleObject childObj = (PermissibleObject) obj;
            childObj.setGlobalRead(hibPermissibleObject.isGlobalRead());
            childObj.setGlobalWrite(hibPermissibleObject.isGlobalWrite());
            childObj.setGlobalExecute(hibPermissibleObject.isGlobalExecute());
            session.save(childObj);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      // save it
      session.save(hibPermissibleObject);
      tx.commit();
      return hibPermissibleObject;
    } catch (Throwable t) {
      t.printStackTrace();
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public void setPermissions(PermissibleObject permissibleObject, List<Permission> permissions) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("PermissibleObject not supplied.");
    }
    if (permissions == null) {
      throw new RuntimeException("Permissions not supplied.");
    }
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.beginTransaction();
    try {
      PermissibleObject hibPermissibleObject = ((PermissibleObject) session.load(PermissibleObject.class, permissibleObject.getId()));
      if (!authUser.isAdministrator() && !hibPermissibleObject.getOwner().equals(authUser)) {
        throw new RuntimeException("User is not authorized to set permissions on this object.");
      }
      session.evict(authUser);

      SecurityHelper.deletePermissions(session, permissibleObject);
      for (Permission permission : permissions) {
        session.save(permission);
      }

      Field fields[] = ReflectionCache.getFields(permissibleObject.getClass());
      for (Field field : fields) {
        try {
          Object obj = field.get(permissibleObject);
          if (obj instanceof PermissibleObject) {
            PermissibleObject childObj = (PermissibleObject) obj;
            childObj.setGlobalRead(permissibleObject.isGlobalRead());
            childObj.setGlobalWrite(permissibleObject.isGlobalWrite());
            childObj.setGlobalExecute(permissibleObject.isGlobalExecute());
            SecurityHelper.deletePermissions(session, childObj);
            for (Permission permission : permissions) {
              Permission newPerm = new Permission();
              newPerm.setPermissibleObject(childObj);
              newPerm.setSecurityPrincipal(permission.getSecurityPrincipal());
              newPerm.setReadPerm(permission.isReadPerm());
              newPerm.setWritePerm(permission.isWritePerm());
              newPerm.setExecutePerm(permission.isExecutePerm());
              session.save(newPerm);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      tx.commit();
    } catch (Throwable t) {
      t.printStackTrace();
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public FileUploadStatus getFileUploadStatus() throws Exception {
    org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    User authUser = getAuthenticatedUser(session);
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    try {
      FileUploadStatus status = fileUploadStatusMap.get(authUser);
      if (status == null) {
        throw new RuntimeException("No stats currently available.");
      }
      return status;
    } catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException(t.getMessage());
    } finally {
      session.close();
    }
  }

  public void createCategory(String categoryName, String categoryDescription, Category parentCategory) throws Exception {
  }

  public void deleteCategory(Category category) throws Exception {
  }

  public List<Category> getCategories() throws Exception {
    return null;
  }

  public void addToCategory(Category category, PermissibleObject permissibleObject) throws Exception {
  }

  public List<Category> getCategories(PermissibleObject permissibleObject) throws Exception {
    return null;
  }

  public void removeFromCategory(Category category, PermissibleObject permissibleObject) throws Exception {
  }

}
