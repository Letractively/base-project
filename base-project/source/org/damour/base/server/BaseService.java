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

  private ThreadLocal<Session> session = new ThreadLocal<Session>();

  public BaseService() {
    super();
  }

  protected void onBeforeRequestDeserialized(String serializedRequest) {
    session.set(HibernateUtil.getInstance().getSession());
    Logger.log(serializedRequest);
  }

  protected void onAfterResponseSerialized(String serializedResponse) {
    try {
      session.get().close();
    } catch (Throwable t) {
    }
    Logger.log(serializedResponse);
  }

  protected void doUnexpectedFailure(Throwable e) {
    try {
      session.get().close();
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

  // protected void emailException(Throwable t) {
  // String trace = Logger.convertThrowableToHTML(t);
  // String from = "admin@" + getDomainName();
  // String to = from;
  // String subject = "A critical server error has occurred.";
  // String message = "<BR/>" + t.getMessage() + "<BR/>" + trace;
  // sendMessage(smtpHost, from, from, to, subject, message);
  // }

  // protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
  // String method = req.getParameter("method");
  // if (method.equalsIgnoreCase("doesUserHavePermission")) {
  // org.hibernate.Session session = HibernateUtil.getInstance().getSession();
  // try {
  // String username = req.getParameter("user");
  // String permissionStr = req.getParameter("permission");
  // Long permissibleObjectId = Long.valueOf(req.getParameter("permissibleObjectId"));
  // User user = UserHelper.getUser(session, username);
  // PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, permissibleObjectId);
  // PERM permission = PERM.valueOf(permissionStr);
  // resp.setContentType("text/xml");
  // resp.getWriter().write("<result>");
  // resp.getWriter().write("" + SecurityHelper.doesUserHavePermission(session, user, permissibleObject, permission));
  // resp.getWriter().write("</result>");
  // } catch (Throwable t) {
  // t.printStackTrace();
  // logException(t);
  // throw new RuntimeException("Invalid doGet request");
  // } finally {
  // session.close();
  // }
  // } else if (method.equals("evictFile")) {
  // org.hibernate.Session session = HibernateUtil.getInstance().getSession();
  // try {
  // Long id = Long.valueOf(req.getParameter("id"));
  // File file = (File) session.load(File.class, id);
  // session.evict(file);
  // session.getSessionFactory().evict(File.class, id);
  // } catch (Throwable t) {
  // logException(t);
  // throw new RuntimeException("Invalid doGet request");
  // } finally {
  // session.close();
  // }
  // }
  // }

  public User login(HttpServletRequest request, HttpServletResponse response, String username, String password) {
    try {
      return login(session.get(), request, response, username, password, false);
    } catch (Throwable t) {
      Logger.log(t);
      throw new RuntimeException("Could not login.  Invalid username or password.");
    }
  }

  public User login(String username, String password) throws Exception {
    try {
      return login(session.get(), getThreadLocalRequest(), getThreadLocalResponse(), username, password, false);
    } catch (Throwable t) {
      Logger.log(t);
      throw new RuntimeException("Could not login.  Invalid username or password.");
    }
  }

  private User login(org.hibernate.Session session, HttpServletRequest request, HttpServletResponse response, String username, String password, boolean internal)
      throws Exception {
    username = username.toLowerCase();
    User user = UserHelper.getUser(session, username);
    MD5 md5 = new MD5();
    md5.Update(password);
    String passwordHash = md5.asHex();
    if (user != null && user.getUsername().equals(username)
        && ((internal && password.equals(user.getPasswordHash())) || user.getPasswordHash().equals(passwordHash))) {
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
    User user = getAuthenticatedUser(session.get());
    if (user == null) {
      destroyCookies(getThreadLocalResponse());
      throw new RuntimeException("Could not get authenticated user.");
    }
    return user;
  }

  public void logout() throws Exception {
    destroyCookies(getThreadLocalResponse());
  }

  // create or edit account
  public User createOrEditAccount(User inUser, String password, String captchaText) throws Exception {
    Transaction tx = session.get().beginTransaction();
    try {
      User possibleAuthUser = getAuthenticatedUser(session.get());
      User authUser = null;
      if (possibleAuthUser instanceof User) {
        authUser = (User) possibleAuthUser;
      }

      User dbUser = null;
      try {
        dbUser = (User) session.get().load(User.class, inUser.getId());
      } catch (Exception e) {
      }

      if (dbUser == null) {
        // new account, it did NOT exist
        // validate captcha first
        if (captchaText != null && !"".equals(captchaText)) {
          Captcha captcha = (Captcha) getThreadLocalRequest().getSession().getAttribute("captcha");
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

        session.get().save(newUser);

        UserGroup userGroup = new UserGroup();
        userGroup.setName(newUser.getUsername());
        userGroup.setOwner(newUser);

        session.get().save(userGroup);

        tx.commit();

        // if we are a true new unauthenticated user, create a new account
        if (authUser == null) {
          destroyCookies(getThreadLocalResponse());
          if (login(session.get(), getThreadLocalRequest(), getThreadLocalResponse(), newUser.getUsername(), newUser.getPasswordHash(), true) != null) {
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

        session.get().save(dbUser);
        tx.commit();

        // if we are editing our own account, then re-authenticate
        if (authUser.getId().equals(dbUser.getId())) {
          destroyCookies(getThreadLocalResponse());
          if (login(session.get(), getThreadLocalRequest(), getThreadLocalResponse(), dbUser.getUsername(), dbUser.getPasswordHash(), true) != null) {
            return dbUser;
          }
        }
        return dbUser;
      }
      throw new RuntimeException("Could not edit account.");
    } catch (Exception ex) {
      Logger.log(ex);
      try {
        tx.rollback();
      } catch (Exception exx) {
      }
      throw new RuntimeException(ex.getMessage());
    }
  }

  public String getLoginHint(String username) throws Exception {
    User user = UserHelper.getUser(session.get(), username.toLowerCase());
    if (user == null) {
      throw new RuntimeException("Could not get login hint.");
    }
    return user.getPasswordHint();
  }

  public List<String> getUsernames() throws Exception {
    // this is a non-admin function
    return SecurityHelper.getUsernames(session.get());
  }

  public List<User> getUsers() throws Exception {
    return SecurityHelper.getUsers(session.get());
  }

  public List<UserGroup> getGroups(User user) throws Exception {
    User authUser = getAuthenticatedUser(session.get());
    // the admin & actual user can list all groups for the user
    if (authUser != null && (authUser.isAdministrator() || authUser.equals(user))) {
      return SecurityHelper.getUserGroups(session.get(), user);
    }
    // everyone else can only see visible groups for the user
    return SecurityHelper.getVisibleUserGroups(session.get(), user);
  }

  public List<UserGroup> getOwnedGroups(User user) throws Exception {
    User authUser = getAuthenticatedUser(session.get());
    // if we are the admin, and we are asking to list owned admin groups,
    // we show all groups to the admin
    if (authUser != null && authUser.isAdministrator()) {
      return SecurityHelper.getUserGroups(session.get());
    }
    // the actual user can list all owned groups for the user
    if (authUser != null && authUser.equals(user)) {
      return SecurityHelper.getOwnedUserGroups(session.get(), user);
    }
    // if we are not the admin or the actual user, we can only list the visible groups
    // to unknown people
    return SecurityHelper.getOwnedVisibleUserGroups(session.get(), user);
  }

  public List<UserGroup> getGroups() throws Exception {
    User authUser = getAuthenticatedUser(session.get());
    // the admin can list all groups
    if (authUser != null && authUser.isAdministrator()) {
      return SecurityHelper.getUserGroups(session.get());
    }
    return SecurityHelper.getVisibleUserGroups(session.get());
  }

  public List<User> getUsers(UserGroup group) throws Exception {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    group = (UserGroup) session.get().load(UserGroup.class, group.getId());
    // only the group owner, group members and administrator can see the users in a group
    if (authUser.isAdministrator() || authUser.equals(group.getOwner())) {
      return SecurityHelper.getUsersInUserGroup(session.get(), group);
    }
    // now check the groups for the user against the group
    List<GroupMembership> memberships = SecurityHelper.getGroupMemberships(session.get(), authUser);
    if (memberships.contains(group)) {
      return SecurityHelper.getUsersInUserGroup(session.get(), group);
    }
    throw new RuntimeException("User is not authorized to list users in group.");
  }

  public GroupMembership addUserToGroup(User user, UserGroup group) throws Exception {
    Transaction tx = null;
    try {
      User authUser = getAuthenticatedUser(session.get());
      if (authUser == null) {
        throw new RuntimeException("Could not join group, attempt to join with unauthorized client.");
      }
      group = (UserGroup) session.get().load(UserGroup.class, group.getId());
      user = (User) session.get().load(User.class, user.getId());

      if (group == null || user == null) {
        throw new RuntimeException("Could not join group, user and group not found.");
      }

      // the group owner and an administrator may add users to groups without obeying the 'lock'
      if (group.isLocked() && !authUser.isAdministrator() && !group.getOwner().getId().equals(authUser.getId())) {
        throw new RuntimeException("This group is currently not accepting new members.");
      }

      if (authUser.isAdministrator() || group.isAutoJoin() || group.getOwner().getId().equals(authUser.getId())) {
        tx = session.get().beginTransaction();
        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setUser(user);
        groupMembership.setUserGroup(group);
        session.get().save(groupMembership);
        tx.commit();
        return groupMembership;
      } else if (!group.isAutoJoin()) {
        tx = session.get().beginTransaction();
        PendingGroupMembership groupMembership = new PendingGroupMembership();
        groupMembership.setUser(user);
        groupMembership.setUserGroup(group);
        session.get().save(groupMembership);
        tx.commit();
        // send email to group owner
        sendMessage(getSmtpHost(), "admin@" + getDomainName(), "admin@" + getDomainName(), group.getOwner().getEmail(), "Group join request from "
            + user.getUsername(), "[" + getDomainName() + "] " + user.getUsername() + " has requested permission to join your group " + group.getName());
        throw new RuntimeException("Could not join group, request submitted to group owner.");
      }
      throw new RuntimeException("Could not join group.");
    } catch (org.hibernate.exception.ConstraintViolationException e) {
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException("Could not join group, user already a member or add request pending.");
    }
  }

  public List<PendingGroupMembership> getPendingGroupMemberships(User user) throws Exception {
    try {
      User authUser = getAuthenticatedUser(session.get());
      if (authUser == null) {
        throw new RuntimeException("Could not join group, attempt to join with unauthorized client.");
      }
      user = (User) session.get().load(User.class, user.getId());

      if (user == null) {
        throw new RuntimeException("Could not get pending groups for supplied user.");
      }

      if (authUser.isAdministrator() || user.getId().equals(authUser.getId())) {
        // remember, administrator owns all
        return SecurityHelper.getPendingGroupMemberships(session.get(), user);
      } else {
        throw new RuntimeException("Could not get pending group memberships.");
      }

    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }

  public List<PendingGroupMembership> submitPendingGroupMembershipApproval(User user, Set<PendingGroupMembership> members, boolean approve) throws Exception {

    if (members == null || members.size() == 0) {
      throw new RuntimeException("List of members provided was empty.");
    }

    if (user == null) {
      throw new RuntimeException("User not supplied.");
    }

    Transaction tx = session.get().beginTransaction();
    try {
      User authUser = getAuthenticatedUser(session.get());
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
              session.get().save(realGroupMembership);
            }
            session.get().delete(pendingGroupMembership);
          }
        }
        tx.commit();
        // send back the new list
        return SecurityHelper.getPendingGroupMemberships(session.get(), user);
      } else {
        throw new RuntimeException("Cannot approve or deny requests without proper authentication.");
      }
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    }
  }

  public UserGroup createOrEditGroup(UserGroup group) throws Exception {
    Transaction tx = session.get().beginTransaction();
    try {
      User authUser = getAuthenticatedUser(session.get());
      if (authUser != null && (authUser.isAdministrator() || authUser.getId().equals(group.getOwner().getId()))) {
        try {
          User owner = (User) session.get().load(User.class, group.getOwner().getId());
          group.setOwner(owner);
        } catch (HibernateException e) {
        }

        if (group.getId() == null) {
          // new group
          // before we save, let's make sure the user doesn't already have a group by this name
          List<UserGroup> existingGroups = SecurityHelper.getOwnedUserGroups(session.get(), group.getOwner());
          for (UserGroup existingGroup : existingGroups) {
            if (existingGroup.getName().equalsIgnoreCase(group.getName())) {
              throw new RuntimeException("A group already exists with this name.");
            }
          }
          session.get().save(group);
          // default is to create membership for the owner
          GroupMembership groupMembership = new GroupMembership();
          groupMembership.setUser(group.getOwner());
          groupMembership.setUserGroup(group);
          session.get().save(groupMembership);
        } else {
          // let's make sure that if we are changing the group name that
          // the only group with this name (for the group owner) is this group
          session.get().saveOrUpdate(group);
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
    }
  }

  public void deleteUser(User user, UserGroup group) throws Exception {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new RuntimeException("Could not remove user from group, attempt made with unauthorized client.");
    }
    group = (UserGroup) session.get().load(UserGroup.class, group.getId());
    user = (User) session.get().load(User.class, user.getId());

    if (group == null || user == null) {
      throw new RuntimeException("Could not remove user from group, user or group not found.");
    }

    if (authUser.isAdministrator() || group.isAutoJoin() || group.getOwner().getId().equals(authUser.getId())) {
      Transaction tx = session.get().beginTransaction();
      GroupMembership groupMembership = SecurityHelper.getGroupMembership(session.get(), user, group);
      if (groupMembership != null) {
        session.get().delete(groupMembership);
      }
      tx.commit();
    }
  }

  public void deleteGroup(UserGroup group) throws Exception {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser != null && (authUser.isAdministrator() || group.getOwner().getId().equals(authUser.getId()))) {
      Transaction tx = session.get().beginTransaction();
      group = (UserGroup) session.get().load(UserGroup.class, group.getId());
      SecurityHelper.deleteUserGroup(session.get(), group);
      tx.commit();
    } else {
      throw new RuntimeException("Could not delete group, insufficient privilidges.");
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
    User authUser = getAuthenticatedUser(session.get());
    if (authUser != null && authUser.isAdministrator()) {
      HibernateUtil.resetHibernate();
    }
  }

  public void evictClassFromCache(String className) throws Exception {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser != null && authUser.isAdministrator()) {
      try {
        Class clazz = Class.forName(className);
        HibernateUtil.getInstance().getSessionFactory().evict(clazz);
        Logger.log("Evicted: " + className);
      } catch (Throwable t) {
        Logger.log(t);
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
    User authUser = getAuthenticatedUser(session.get());
    if (authUser != null && authUser.isAdministrator()) {
      try {
        System.gc();
      } catch (Throwable t) {
        Logger.log(t);
      }
    }
    return getMemoryStats();
  }

  public Date getServerStartupDate() throws Exception {
    return new Date(BaseSystem.getStartupDate());
  }

  public UserRating getUserRating(PermissibleObject permissibleObject) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("PermissibleObject not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      permissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId());
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
        throw new RuntimeException("User is not authorized to get rating on this content.");
      }
      // find rating based on remote address if needed
      return RatingHelper.getUserRating(session.get(), permissibleObject, authUser, getThreadLocalRequest().getRemoteAddr());
    } catch (Throwable t) {
      Logger.log(t);
      throw new RuntimeException(t.getMessage());
    }
  }

  public UserRating setUserRating(PermissibleObject permissibleObject, int rating) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("PermissibleObject not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    Transaction tx = session.get().beginTransaction();
    try {
      permissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId());

      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
        throw new RuntimeException("User is not authorized to set rating on this content.");
      }

      float totalRating = (float) permissibleObject.getNumRatingVotes() * permissibleObject.getAverageRating();
      totalRating += rating;
      permissibleObject.setNumRatingVotes(permissibleObject.getNumRatingVotes() + 1);
      float newAvg = totalRating / (float) permissibleObject.getNumRatingVotes();
      permissibleObject.setAverageRating(newAvg);
      session.get().save(permissibleObject);

      UserRating userRating = RatingHelper.getUserRating(session.get(), permissibleObject, authUser, getThreadLocalRequest().getRemoteAddr());

      if (userRating != null) {
        throw new RuntimeException("Already voted.");
      }

      // check if rating already exists
      userRating = new UserRating();
      userRating.setPermissibleObject(permissibleObject);
      userRating.setRating(rating);
      userRating.setVoter(authUser);
      userRating.setVoterIP(getThreadLocalRequest().getRemoteAddr());

      session.get().save(userRating);
      tx.commit();
      return userRating;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    }
  }

  public UserAdvisory getUserAdvisory(PermissibleObject permissibleObject) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("PermissibleObject not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      permissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId());

      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
        throw new RuntimeException("User is not authorized to get advisory on this content.");
      }
      // find rating based on remote address if needed
      return AdvisoryHelper.getUserAdvisory(session.get(), permissibleObject, authUser, getThreadLocalRequest().getRemoteAddr());
    } catch (Throwable t) {
      Logger.log(t);
      throw new RuntimeException(t.getMessage());
    }
  }

  public UserAdvisory setUserAdvisory(PermissibleObject permissibleObject, int advisory) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("PermissibleObject not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    Transaction tx = session.get().beginTransaction();
    try {
      permissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId());

      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
        throw new RuntimeException("User is not authorized to set advisory on this content.");
      }

      float totalAdvisory = (float) permissibleObject.getNumAdvisoryVotes() * permissibleObject.getAverageAdvisory();
      totalAdvisory += advisory;
      permissibleObject.setNumAdvisoryVotes(permissibleObject.getNumAdvisoryVotes() + 1);
      float newAvg = totalAdvisory / (float) permissibleObject.getNumAdvisoryVotes();
      permissibleObject.setAverageAdvisory(newAvg);
      session.get().save(permissibleObject);

      UserAdvisory userAdvisory = AdvisoryHelper.getUserAdvisory(session.get(), permissibleObject, authUser, getThreadLocalRequest().getRemoteAddr());

      if (userAdvisory != null) {
        throw new RuntimeException("Already voted.");
      }

      // check if rating already exists
      userAdvisory = new UserAdvisory();
      userAdvisory.setPermissibleObject(permissibleObject);
      userAdvisory.setRating(advisory);
      userAdvisory.setVoter(authUser);
      userAdvisory.setVoterIP(getThreadLocalRequest().getRemoteAddr());

      session.get().save(userAdvisory);
      tx.commit();
      return userAdvisory;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    }
  }

  public List<Comment> getComments(PermissibleObject permissibleObject) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("File not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      permissibleObject = ((PermissibleObject) session.get().load(File.class, permissibleObject.getId()));
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
        throw new RuntimeException("User is not authorized to get comments on this content.");
      }
      return CommentHelper.getComments(session.get(), permissibleObject);
    } catch (Throwable t) {
      Logger.log(t);
      throw new RuntimeException(t.getMessage());
    }
  }

  public Page<Comment> getCommentPage(PermissibleObject permissibleObject, boolean sortDescending, int pageNumber, int pageSize) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("File not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      permissibleObject = ((PermissibleObject) session.get().load(File.class, permissibleObject.getId()));
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
        throw new RuntimeException("User is not authorized to get comments on this content.");
      }
      GenericPage<Comment> gPage = new GenericPage<Comment>(session.get(), "from " + Comment.class.getSimpleName() + " where permissibleObject.id = "
          + permissibleObject.getId() + " order by id " + (sortDescending ? "desc" : "asc"), pageNumber, pageSize);
      return new Page<Comment>(gPage.getList(), pageNumber, gPage.getLastPageNumber(), gPage.getRowCount());
    } catch (Throwable t) {
      Logger.log(t);
      throw new RuntimeException(t.getMessage());
    }
  }

  public Boolean submitComment(Comment comment) throws Exception {
    if (comment == null) {
      throw new RuntimeException("Comment not supplied.");
    }
    if (comment.getPermissibleObject() == null) {
      throw new RuntimeException("PermissibleObject not supplied with comment.");
    }
    User authUser = getAuthenticatedUser(session.get());
    Transaction tx = session.get().beginTransaction();
    try {
      comment.setPermissibleObject((PermissibleObject) session.get().load(PermissibleObject.class, comment.getPermissibleObject().getId()));
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, comment.getPermissibleObject(), PERM.READ)) {
        throw new RuntimeException("User is not authorized to make comments on this content.");
      }
      if (!comment.permissibleObject.isAllowComments()) {
        throw new RuntimeException("Comments are not allowed on this content.");
      }
      // the comment is approved if we are not moderating or if the commenter is the file owner
      comment.setApproved(!comment.permissibleObject.isModerateComments() || comment.getPermissibleObject().getOwner().equals(authUser));
      session.get().save(comment);
      tx.commit();
      return true;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    }
  }

  public Boolean approveComment(Comment comment) throws Exception {
    if (comment == null) {
      throw new RuntimeException("Comment not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new RuntimeException(".");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      comment = ((Comment) session.get().load(Comment.class, comment.getId()));
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, comment.getPermissibleObject(), PERM.WRITE)) {
        throw new RuntimeException("User is not authorized to approve comments for this content.");
      }
      comment.setApproved(true);
      session.get().save(comment);
      tx.commit();
      return true;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    }
  }

  public Boolean deleteComment(Comment comment) throws Exception {
    if (comment == null) {
      throw new RuntimeException("Comment not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      comment = ((Comment) session.get().load(Comment.class, comment.getId()));
      if (!comment.getAuthor().equals(authUser) && !SecurityHelper.doesUserHavePermission(session.get(), authUser, comment.getPermissibleObject(), PERM.WRITE)) {
        throw new RuntimeException("User is not authorized to delete comments for this content.");
      }
      // we can't delete this comment until we delete all the child comments
      CommentHelper.deleteComment(session.get(), comment);
      tx.commit();
      return true;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    }
  }

  public File getFile(Long id) throws Exception {
    if (id == null) {
      throw new RuntimeException("Id not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      File file = (File) session.get().load(File.class, id);
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, file, PERM.READ)) {
        throw new RuntimeException("User is not authorized to get this content.");
      }
      return file;
    } catch (Throwable t) {
      Logger.log(t);
      throw new RuntimeException(t.getMessage());
    }
  }

  public RepositoryTreeNode getRepositoryTree() throws Exception {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    try {
      RepositoryTreeNode root = new RepositoryTreeNode();
      RepositoryHelper.buildRepositoryTreeNode(session.get(), authUser, root, null);
      return root;
    } catch (Throwable t) {
      Logger.log(t);
      throw new RuntimeException(t.getMessage());
    }
  }

  public Folder createNewFolder(Folder newFolder) throws Exception {
    if (newFolder == null) {
      throw new RuntimeException("Folder not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      if (newFolder.getParent() != null) {
        newFolder.setParent((PermissibleObject) session.get().load(PermissibleObject.class, newFolder.getParent().getId()));
      }
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, newFolder.getParent(), PERM.WRITE)) {
        throw new RuntimeException("User is not authorized to create a new folder here.");
      }
      if (newFolder.getId() != null) {
        Folder hibNewFolder = (Folder) session.get().load(Folder.class, newFolder.getId());
        if (hibNewFolder != null) {
          if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, hibNewFolder, PERM.WRITE)) {
            throw new RuntimeException("User is not authorized to save a new folder here.");
          }
          hibNewFolder.setName(newFolder.getName());
          hibNewFolder.setDescription(newFolder.getDescription());
          hibNewFolder.setParent(newFolder.getParent());
          newFolder = hibNewFolder;
        }
      }

      newFolder.setOwner(authUser);
      session.get().save(newFolder);
      tx.commit();
      return newFolder;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    }
  }

  public void deleteFile(File file) throws Exception {
    if (file == null) {
      throw new RuntimeException("File not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      file = (File) session.get().load(File.class, file.getId());
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, file, PERM.WRITE)) {
        throw new RuntimeException("User is not authorized to delete this file.");
      }
      FileObjectHelper.deleteFile(session.get(), file);
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    }
  }

  public void deleteFolder(Folder folder) throws Exception {
    if (folder == null) {
      throw new RuntimeException("Folder not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      folder = (Folder) session.get().load(Folder.class, folder.getId());
      if (!authUser.isAdministrator() && !authUser.equals(folder.getOwner())) {
        throw new RuntimeException("User is not authorized to delete this folder.");
      }
      FolderHelper.deleteFolder(session.get(), folder);
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    }
  }

  public void renameFile(File file) throws Exception {
    if (file == null) {
      throw new RuntimeException("File not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      File hibfile = (File) session.get().load(File.class, file.getId());
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, hibfile, PERM.WRITE)) {
        throw new RuntimeException("User is not authorized to rename this file.");
      }
      hibfile.setName(file.getName());
      session.get().save(hibfile);
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    }
  }

  public void renameFolder(Folder folder) throws Exception {
    if (folder == null) {
      throw new RuntimeException("Folder not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      Folder hibfolder = (Folder) session.get().load(Folder.class, folder.getId());
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, hibfolder, PERM.WRITE)) {
        throw new RuntimeException("User is not authorized to rename this folder.");
      }
      hibfolder.setName(folder.getName());
      session.get().save(hibfolder);
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    }
  }

  public List<Permission> getPermissions(PermissibleObject permissibleObject) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("PermissibleObject not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    try {
      permissibleObject = ((PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId()));
      if (!authUser.isAdministrator() && !permissibleObject.getOwner().equals(authUser)) {
        throw new RuntimeException("User is not authorized to get permissions on this content.");
      }
      return SecurityHelper.getPermissions(session.get(), permissibleObject);
    } catch (Throwable t) {
      Logger.log(t);
      throw new RuntimeException(t.getMessage());
    }
  }

  public PermissibleObject updatePermissibleObject(PermissibleObject permissibleObject) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("PermissibleObject not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      User newOwner = ((User) session.get().load(User.class, permissibleObject.getOwner().getId()));
      PermissibleObject hibPermissibleObject = ((PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId()));
      if (!authUser.isAdministrator() && !hibPermissibleObject.getOwner().equals(authUser)) {
        throw new RuntimeException("User is not authorized to update this object.");
      }
      // otherwise hibernate will be upset:
      hibPermissibleObject.setName(permissibleObject.getName());
      hibPermissibleObject.setOwner(newOwner);
      hibPermissibleObject.setGlobalRead(permissibleObject.isGlobalRead());
      hibPermissibleObject.setGlobalWrite(permissibleObject.isGlobalWrite());
      hibPermissibleObject.setGlobalExecute(permissibleObject.isGlobalExecute());

      // update 'child' fields (for example, image has child permissibles)
      Field fields[] = ReflectionCache.getFields(hibPermissibleObject.getClass());
      for (Field field : fields) {
        try {
          Object obj = field.get(hibPermissibleObject);
          if (obj instanceof PermissibleObject) {
            PermissibleObject childObj = (PermissibleObject) obj;
            childObj.setGlobalRead(hibPermissibleObject.isGlobalRead());
            childObj.setGlobalWrite(hibPermissibleObject.isGlobalWrite());
            childObj.setGlobalExecute(hibPermissibleObject.isGlobalExecute());
            session.get().save(childObj);
          }
        } catch (Exception e) {
          Logger.log(e);
        }
      }

      // save it
      session.get().save(hibPermissibleObject);
      tx.commit();
      return hibPermissibleObject;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    }
  }

  public void setPermissions(PermissibleObject permissibleObject, List<Permission> permissions) throws Exception {
    if (permissibleObject == null) {
      throw new RuntimeException("PermissibleObject not supplied.");
    }
    if (permissions == null) {
      throw new RuntimeException("Permissions not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new RuntimeException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      PermissibleObject hibPermissibleObject = ((PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId()));
      if (!authUser.isAdministrator() && !hibPermissibleObject.getOwner().equals(authUser)) {
        throw new RuntimeException("User is not authorized to set permissions on this object.");
      }
      session.get().evict(authUser);

      SecurityHelper.deletePermissions(session.get(), permissibleObject);
      for (Permission permission : permissions) {
        session.get().save(permission);
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
            SecurityHelper.deletePermissions(session.get(), childObj);
            for (Permission permission : permissions) {
              Permission newPerm = new Permission();
              newPerm.setPermissibleObject(childObj);
              newPerm.setSecurityPrincipal(permission.getSecurityPrincipal());
              newPerm.setReadPerm(permission.isReadPerm());
              newPerm.setWritePerm(permission.isWritePerm());
              newPerm.setExecutePerm(permission.isExecutePerm());
              session.get().save(newPerm);
            }
          }
        } catch (Exception e) {
          Logger.log(e);
        }
      }
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new RuntimeException(t.getMessage());
    }
  }

  public FileUploadStatus getFileUploadStatus() throws Exception {
    User authUser = getAuthenticatedUser(session.get());
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
      Logger.log(t);
      throw new RuntimeException(t.getMessage());
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