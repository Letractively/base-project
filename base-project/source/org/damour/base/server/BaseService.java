package org.damour.base.server;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.damour.base.client.exceptions.LoginException;
import org.damour.base.client.exceptions.SimpleMessageException;
import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.FileUploadStatus;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.HibernateStat;
import org.damour.base.client.objects.IAnonymousPermissibleObject;
import org.damour.base.client.objects.MemoryStats;
import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PageInfo;
import org.damour.base.client.objects.PendingGroupMembership;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.Referral;
import org.damour.base.client.objects.Permission.PERM;
import org.damour.base.client.objects.RepositoryTreeNode;
import org.damour.base.client.objects.Tag;
import org.damour.base.client.objects.TagMembership;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserAdvisory;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.objects.UserRating;
import org.damour.base.client.objects.UserThumb;
import org.damour.base.client.utils.StringUtils;
import org.damour.base.server.gwt.RemoteServiceServlet;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.ReflectionCache;
import org.damour.base.server.hibernate.helpers.AdvisoryHelper;
import org.damour.base.server.hibernate.helpers.CommentHelper;
import org.damour.base.server.hibernate.helpers.FolderHelper;
import org.damour.base.server.hibernate.helpers.PageHelper;
import org.damour.base.server.hibernate.helpers.PermissibleObjectHelper;
import org.damour.base.server.hibernate.helpers.RatingHelper;
import org.damour.base.server.hibernate.helpers.RepositoryHelper;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.damour.base.server.hibernate.helpers.TagHelper;
import org.damour.base.server.hibernate.helpers.ThumbHelper;
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

  private ThreadLocal<Session> session = new ThreadLocal<Session>();

  public Session getSession() {
    return session.get();
  }

  public BaseService() {
    super();
  }

  protected void onBeforeRequestDeserialized(String serializedRequest) {
    session.set(HibernateUtil.getInstance().getSession());
    BaseSystem.getDomainName(getThreadLocalRequest());
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

  public User login(HttpServletRequest request, HttpServletResponse response, String username, String password) {
    try {
      return login(session.get(), request, response, username, password, false);
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException("Could not login.  Invalid username or password.");
    }
  }

  public User login(String username, String password) throws SimpleMessageException {
    try {
      return login(session.get(), getThreadLocalRequest(), getThreadLocalResponse(), username, password, false);
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  private boolean isAccountValidated(User user) {
    if (!BaseSystem.requireAccountValidation()) {
      return true;
    }
    if (user == null) {
      return false;
    }
    return user.isValidated();
  }

  private User login(org.hibernate.Session session, HttpServletRequest request, HttpServletResponse response, String username, String password, boolean internal)
      throws SimpleMessageException {
    username = username.toLowerCase();
    User user = UserHelper.getUser(session, username);
    MD5 md5 = new MD5();
    md5.Update(password);
    String passwordHash = md5.asHex();
    if (user != null && isAccountValidated(user) && ((internal && password.equals(user.getPasswordHash())) || user.getPasswordHash().equals(passwordHash))) {
      Cookie userCookie = new Cookie("user", user.getUsername());
      userCookie.setPath("/");
      userCookie.setMaxAge(COOKIE_TIMEOUT);
      Cookie userAuthCookie = new Cookie("auth", internal ? password : passwordHash);
      userAuthCookie.setPath("/");
      userAuthCookie.setMaxAge(COOKIE_TIMEOUT);
      Cookie voterCookie = new Cookie("voterGUID", UUID.randomUUID().toString());
      voterCookie.setPath("/");
      voterCookie.setMaxAge(COOKIE_TIMEOUT);
      response.addCookie(userCookie);
      response.addCookie(userAuthCookie);
      response.addCookie(voterCookie);
    } else {
      destroyAuthCookies(request, response);
      if (user != null && !isAccountValidated(user)) {
        throw new SimpleMessageException("Could not login.  Account is not validated.");
      }
      throw new SimpleMessageException("Could not login.  Invalid username or password.");
    }
    return user;
  }

  private void destroyAuthCookies(HttpServletRequest request, HttpServletResponse response) {
    Cookie userCookie = new Cookie("user", "");
    userCookie.setMaxAge(0);
    userCookie.setPath("/");
    Cookie userAuthCookie = new Cookie("auth", "");
    userAuthCookie.setMaxAge(0);
    userAuthCookie.setPath("/");
    response.addCookie(userCookie);
    response.addCookie(userAuthCookie);
  }

  private void destroyAllCookies(HttpServletRequest request, HttpServletResponse response) {
    for (Cookie cookie : getThreadLocalRequest().getCookies()) {
      cookie.setMaxAge(0);
      cookie.setPath("/");
      getThreadLocalResponse().addCookie(cookie);
    }
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

  protected User getAuthenticatedUser(org.hibernate.Session session) throws LoginException {
    return getAuthenticatedUser(session, getThreadLocalRequest(), getThreadLocalResponse());
  }

  public User getAuthenticatedUser() throws LoginException {
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
      throw new LoginException("Could not get authenticated user.");
    }
    User user = getAuthenticatedUser(session.get());
    if (user == null) {
      destroyAuthCookies(getThreadLocalRequest(), getThreadLocalResponse());
      throw new LoginException("Could not get authenticated user.");
    }
    return user;
  }

  public void logout() throws SimpleMessageException {
    destroyAllCookies(getThreadLocalRequest(), getThreadLocalResponse());
  }

  // create or edit account
  public User createOrEditAccount(User inUser, String password, String captchaText) throws SimpleMessageException {
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
        if (StringUtils.isEmpty(captchaText)) {
          captchaText = "INVALID!";
        }
        Captcha captcha = (Captcha) getThreadLocalRequest().getSession().getAttribute("captcha");
        if (captcha != null && !captcha.isValid(captchaText)) {
          throw new SimpleMessageException("CAPTCHA validation failed");
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

        newUser.setValidated(!BaseSystem.requireAccountValidation());
        if (authUser != null && authUser.isAdministrator()) {
          // admin can automatically create/validate accounts
          newUser.setValidated(true);
        }

        session.get().save(newUser);

        UserGroup userGroup = new UserGroup();
        userGroup.setName(newUser.getUsername());
        userGroup.setVisible(true);
        userGroup.setAutoJoin(false);
        userGroup.setLocked(false);
        userGroup.setOwner(newUser);

        session.get().save(userGroup);

        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setUser(newUser);
        groupMembership.setUserGroup(userGroup);
        session.get().save(groupMembership);

        tx.commit();

        // if a new user is creating a new account, login if new user account is validated
        if (authUser == null && isAccountValidated(newUser)) {
          destroyAuthCookies(getThreadLocalRequest(), getThreadLocalResponse());
          if (login(session.get(), getThreadLocalRequest(), getThreadLocalResponse(), newUser.getUsername(), newUser.getPasswordHash(), true) != null) {
            return newUser;
          }
        } else if (authUser == null && !isAccountValidated(newUser)) {
          // send user a validation email, where, upon clicking the link, their account will be validated
          // the validation code in the URL will simply be a hash of their email address
          MD5 md5 = new MD5();
          md5.Update(newUser.getEmail());
          md5.Update(newUser.getPasswordHash());

          String portStr = "";
          if (getThreadLocalRequest().getLocalPort() != 80) {
            portStr = ":" + getThreadLocalRequest().getLocalPort();
          }
          String url = getThreadLocalRequest().getScheme() + "://" + getThreadLocalRequest().getServerName() + portStr + "/?u=" + newUser.getUsername() + "&v="
              + md5.asHex();

          String text = "Thank you for signing up with " + BaseSystem.getDomainName()
              + ".<BR><BR>Please confirm your account by clicking the following link:<BR><BR>";
          text += "<A HREF=\"";
          text += url;
          text += "\">" + url + "</A>";
          BaseSystem.getEmailService().sendMessage(BaseSystem.getSmtpHost(), BaseSystem.getAdminEmailAddress(), BaseSystem.getDomainName() + " validator",
              newUser.getEmail(), BaseSystem.getDomainName() + " account validation", text);
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

        // only admin can validate directly
        if (authUser.isAdministrator()) {
          dbUser.setValidated(inUser.isValidated());
        }

        session.get().save(dbUser);
        tx.commit();

        // if we are editing our own account, then re-authenticate
        if (authUser.getId().equals(dbUser.getId())) {
          destroyAuthCookies(getThreadLocalRequest(), getThreadLocalResponse());
          if (login(session.get(), getThreadLocalRequest(), getThreadLocalResponse(), dbUser.getUsername(), dbUser.getPasswordHash(), true) != null) {
            return dbUser;
          }
        }
        return dbUser;
      }
      throw new SimpleMessageException("Could not edit account.");
    } catch (Exception ex) {
      Logger.log(ex);
      try {
        tx.rollback();
      } catch (Exception exx) {
      }
      if (ex.getCause() != null) {
        throw new SimpleMessageException(ex.getCause().getMessage());
      } else {
        throw new SimpleMessageException(ex.getMessage());
      }
    }
  }

  public String getLoginHint(String username) throws SimpleMessageException {
    User user = UserHelper.getUser(session.get(), username.toLowerCase());
    if (user == null) {
      throw new SimpleMessageException("Could not get login hint.");
    }
    return user.getPasswordHint();
  }

  public List<String> getUsernames() throws SimpleMessageException {
    // this is a non-admin function
    return SecurityHelper.getUsernames(session.get());
  }

  public List<User> getUsers() throws SimpleMessageException {
    return SecurityHelper.getUsers(session.get());
  }

  public List<UserGroup> getGroups(User user) throws SimpleMessageException {
    User authUser = getAuthenticatedUser(session.get());
    // the admin & actual user can list all groups for the user
    if (authUser != null && (authUser.isAdministrator() || authUser.equals(user))) {
      return SecurityHelper.getUserGroups(session.get(), user);
    }
    // everyone else can only see visible groups for the user
    return SecurityHelper.getVisibleUserGroups(session.get(), user);
  }

  public List<UserGroup> getOwnedGroups(User user) throws SimpleMessageException {
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

  public List<UserGroup> getGroups() throws SimpleMessageException {
    User authUser = getAuthenticatedUser(session.get());
    // the admin can list all groups
    if (authUser != null && authUser.isAdministrator()) {
      return SecurityHelper.getUserGroups(session.get());
    }
    return SecurityHelper.getVisibleUserGroups(session.get());
  }

  public List<User> getUsers(UserGroup group) throws SimpleMessageException {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
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
    throw new SimpleMessageException("User is not authorized to list users in group.");
  }

  public GroupMembership addUserToGroup(User user, UserGroup group) throws SimpleMessageException {
    Transaction tx = null;
    try {
      User authUser = getAuthenticatedUser(session.get());
      if (authUser == null) {
        throw new SimpleMessageException("Could not join group, attempt to join with unauthorized client.");
      }
      group = (UserGroup) session.get().load(UserGroup.class, group.getId());
      user = (User) session.get().load(User.class, user.getId());

      if (group == null || user == null) {
        throw new SimpleMessageException("Could not join group, user and group not found.");
      }

      // the group owner and an administrator may add users to groups without obeying the 'lock'
      if (group.isLocked() && !authUser.isAdministrator() && !group.getOwner().getId().equals(authUser.getId())) {
        throw new SimpleMessageException("This group is currently not accepting new members.");
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
        BaseSystem.getEmailService().sendMessage(BaseSystem.getSmtpHost(), BaseSystem.getAdminEmailAddress(), BaseSystem.getAdminEmailAddress(),
            group.getOwner().getEmail(), "Group join request from " + user.getUsername(),
            "[" + BaseSystem.getDomainName() + "] " + user.getUsername() + " has requested permission to join your group " + group.getName());
        throw new SimpleMessageException("Could not join group, request submitted to group owner.");
      }
      throw new SimpleMessageException("Could not join group.");
    } catch (org.hibernate.exception.ConstraintViolationException e) {
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException("Could not join group, user already a member or add request pending.");
    }
  }

  public List<PendingGroupMembership> getPendingGroupMemberships(User user) throws SimpleMessageException {
    try {
      User authUser = getAuthenticatedUser(session.get());
      if (authUser == null) {
        throw new SimpleMessageException("Could not join group, attempt to join with unauthorized client.");
      }
      user = (User) session.get().load(User.class, user.getId());

      if (user == null) {
        throw new SimpleMessageException("Could not get pending groups for supplied user.");
      }

      if (authUser.isAdministrator() || user.getId().equals(authUser.getId())) {
        // remember, administrator owns all
        return SecurityHelper.getPendingGroupMemberships(session.get(), user);
      } else {
        throw new SimpleMessageException("Could not get pending group memberships.");
      }

    } catch (Throwable t) {
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<PendingGroupMembership> submitPendingGroupMembershipApproval(User user, Set<PendingGroupMembership> members, boolean approve)
      throws SimpleMessageException {

    if (members == null || members.size() == 0) {
      throw new SimpleMessageException("List of members provided was empty.");
    }

    if (user == null) {
      throw new SimpleMessageException("User not supplied.");
    }

    Transaction tx = session.get().beginTransaction();
    try {
      User authUser = getAuthenticatedUser(session.get());
      if (authUser == null) {
        throw new SimpleMessageException("Cannot approve or deny requests without authentication.");
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
        throw new SimpleMessageException("Cannot approve or deny requests without proper authentication.");
      }
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public UserGroup createOrEditGroup(UserGroup group) throws SimpleMessageException {
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
              throw new SimpleMessageException("A group already exists with this name.");
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
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public void deleteUser(User user, UserGroup group) throws SimpleMessageException {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("Could not remove user from group, attempt made with unauthorized client.");
    }
    group = (UserGroup) session.get().load(UserGroup.class, group.getId());
    user = (User) session.get().load(User.class, user.getId());

    if (group == null || user == null) {
      throw new SimpleMessageException("Could not remove user from group, user or group not found.");
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

  public void deleteGroup(UserGroup group) throws SimpleMessageException {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser != null && (authUser.isAdministrator() || group.getOwner().getId().equals(authUser.getId()))) {
      Transaction tx = session.get().beginTransaction();
      group = (UserGroup) session.get().load(UserGroup.class, group.getId());
      SecurityHelper.deleteUserGroup(session.get(), group);
      tx.commit();
    } else {
      throw new SimpleMessageException("Could not delete group, insufficient privilidges.");
    }
  }

  public List<HibernateStat> getHibernateStats() throws SimpleMessageException {
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

  public void resetHibernate() throws SimpleMessageException {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser != null && authUser.isAdministrator()) {
      HibernateUtil.resetHibernate();
    }
  }

  public void evictClassFromCache(String className) throws SimpleMessageException {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser != null && authUser.isAdministrator()) {
      try {
        Class<?> clazz = Class.forName(className);
        HibernateUtil.getInstance().getSessionFactory().evict(clazz);
        Logger.log("Evicted: " + className);
      } catch (Throwable t) {
        Logger.log(t);
      }
    }
  }

  public MemoryStats getMemoryStats() throws SimpleMessageException {
    MemoryStats stats = new MemoryStats();
    stats.setMaxMemory(Runtime.getRuntime().maxMemory());
    stats.setTotalMemory(Runtime.getRuntime().totalMemory());
    stats.setFreeMemory(Runtime.getRuntime().freeMemory());
    return stats;
  }

  public MemoryStats requestGarbageCollection() throws SimpleMessageException {
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

  public Date getServerStartupDate() throws SimpleMessageException {
    return new Date(BaseSystem.getStartupDate());
  }

  public void ping() {
    // NOOP
    Logger.log("Ping received from: " + getThreadLocalRequest().getRemoteAddr());
  }

  public String executeHQL(String query, boolean executeUpdate) {
    if (StringUtils.isEmpty(query)) {
      throw new SimpleMessageException("Query not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null || !authUser.isAdministrator()) {
      throw new SimpleMessageException("Insufficient authorization.");
    }
    Transaction tx = null;
    try {
      tx = session.get().beginTransaction();
      String result;
      if (executeUpdate) {
        result = "Rows affected: " + session.get().createQuery(query).executeUpdate();
      } else {
        List<?> list = session.get().createQuery(query).list();
        result = "Objects returned: " + list.size() + "\r\n";
        for (int i = 0; i < list.size(); i++) {
          result += "Obj[" + i + "]: " + list.get(i).toString() + "\r\n";
        }
      }

      tx.commit();
      return result;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public UserRating getUserRating(PermissibleObject permissibleObject) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      permissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId());
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
        throw new SimpleMessageException("User is not authorized to get rating on this content.");
      }
      // find rating based on remote address if needed
      return RatingHelper.getUserRating(session.get(), permissibleObject, authUser, getVoterGUID());
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public UserRating setUserRating(PermissibleObject permissibleObject, int rating) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    Transaction tx = session.get().beginTransaction();
    try {
      permissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId());

      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
        throw new SimpleMessageException("User is not authorized to set rating on this content.");
      }

      UserRating userRating = RatingHelper.getUserRating(session.get(), permissibleObject, authUser, getVoterGUID());
      // check if rating already exists
      if (userRating != null) {
        // TODO: consider changing the vote
        // simply subtract the previous amount and decrement the numRatingVotes and redivide
        throw new SimpleMessageException("Already voted.");
      }

      float totalRating = (float) permissibleObject.getNumRatingVotes() * permissibleObject.getAverageRating();
      totalRating += rating;
      permissibleObject.setNumRatingVotes(permissibleObject.getNumRatingVotes() + 1);
      float newAvg = totalRating / (float) permissibleObject.getNumRatingVotes();
      permissibleObject.setAverageRating(newAvg);
      session.get().save(permissibleObject);

      userRating = new UserRating();
      userRating.setPermissibleObject(permissibleObject);
      userRating.setRating(rating);
      userRating.setVoter(authUser);
      userRating.setVoterGUID(getVoterGUID());

      session.get().save(userRating);
      tx.commit();
      return userRating;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public UserThumb getUserThumb(PermissibleObject permissibleObject) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      permissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId());
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
        throw new SimpleMessageException("User is not authorized to get thumbs on this content.");
      }
      // find thumb based on remote address if needed
      return ThumbHelper.getUserThumb(session.get(), permissibleObject, authUser, getVoterGUID());
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public UserThumb setUserThumb(PermissibleObject permissibleObject, boolean like) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    Transaction tx = session.get().beginTransaction();
    try {
      permissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId());

      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
        throw new SimpleMessageException("User is not authorized to set thumbs on this content.");
      }

      UserThumb userThumb = ThumbHelper.getUserThumb(session.get(), permissibleObject, authUser, getVoterGUID());
      // check if thumb already exists
      if (userThumb != null) {
        // TODO: consider changing the vote
        // simply subtract the previous amount and decrement the numRatingVotes and redivide
        throw new SimpleMessageException("Already voted.");
      }

      if (like) {
        permissibleObject.setNumUpVotes(permissibleObject.getNumUpVotes() + 1);
      } else {
        permissibleObject.setNumDownVotes(permissibleObject.getNumDownVotes() + 1);
      }
      session.get().save(permissibleObject);

      userThumb = new UserThumb();
      userThumb.setPermissibleObject(permissibleObject);
      userThumb.setLikeThumb(like);
      userThumb.setVoter(authUser);
      userThumb.setVoterGUID(getVoterGUID());

      session.get().save(userThumb);
      tx.commit();
      return userThumb;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<PermissibleObject> getMostRated(int maxResults, String classType) throws SimpleMessageException {
    if (classType == null) {
      throw new SimpleMessageException("classType not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      List<PermissibleObject> mostRated = new ArrayList<PermissibleObject>();
      String simpleClassName = Class.forName(classType).getSimpleName();
      List<PermissibleObject> list = session.get().createQuery("from " + simpleClassName + " where numRatingVotes > 0 order by numRatingVotes desc")
          .setMaxResults(maxResults).setCacheable(true).list();
      for (PermissibleObject permissibleObject : list) {
        if (SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
          mostRated.add(permissibleObject);
        }
      }
      return mostRated;
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<PermissibleObject> getTopRated(int maxResults, int minNumVotes, String classType) throws SimpleMessageException {
    if (classType == null) {
      throw new SimpleMessageException("classType not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      List<PermissibleObject> mostRated = new ArrayList<PermissibleObject>();
      String simpleClassName = Class.forName(classType).getSimpleName();
      List<PermissibleObject> list = session.get()
          .createQuery("from " + simpleClassName + " where numRatingVotes >= " + minNumVotes + " order by averageRating desc").setMaxResults(maxResults)
          .setCacheable(true).list();
      for (PermissibleObject permissibleObject : list) {
        if (SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
          mostRated.add(permissibleObject);
        }
      }
      return mostRated;
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<PermissibleObject> getBottomRated(int maxResults, int minNumVotes, String classType) throws SimpleMessageException {
    if (classType == null) {
      throw new SimpleMessageException("classType not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      List<PermissibleObject> mostRated = new ArrayList<PermissibleObject>();
      String simpleClassName = Class.forName(classType).getSimpleName();
      List<PermissibleObject> list = session.get()
          .createQuery("from " + simpleClassName + " where numRatingVotes >= " + minNumVotes + " order by averageRating asc").setMaxResults(maxResults)
          .setCacheable(true).list();
      for (PermissibleObject permissibleObject : list) {
        if (SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
          mostRated.add(permissibleObject);
        }
      }
      return mostRated;
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<PermissibleObject> getMostLiked(int maxResults, int minNumVotes, String classType) throws SimpleMessageException {
    if (classType == null) {
      throw new SimpleMessageException("classType not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      List<PermissibleObject> mostRated = new ArrayList<PermissibleObject>();
      String simpleClassName = Class.forName(classType).getSimpleName();
      List<PermissibleObject> list = session.get().createQuery("from " + simpleClassName + " where numUpVotes >= " + minNumVotes + " order by numUpVotes desc")
          .setMaxResults(maxResults).setCacheable(true).list();
      for (PermissibleObject permissibleObject : list) {
        if (SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
          mostRated.add(permissibleObject);
        }
      }
      return mostRated;
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<PermissibleObject> getMostDisliked(int maxResults, int minNumVotes, String classType) throws SimpleMessageException {
    if (classType == null) {
      throw new SimpleMessageException("classType not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      List<PermissibleObject> mostRated = new ArrayList<PermissibleObject>();
      String simpleClassName = Class.forName(classType).getSimpleName();
      List<PermissibleObject> list = session.get()
          .createQuery("from " + simpleClassName + " where numDownVotes >= " + minNumVotes + " order by numDownVotes desc").setMaxResults(maxResults)
          .setCacheable(true).list();
      for (PermissibleObject permissibleObject : list) {
        if (SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
          mostRated.add(permissibleObject);
        }
      }
      return mostRated;
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<PermissibleObject> getCreatedSince(int maxResults, long sinceDateMillis, String classType) throws SimpleMessageException {
    if (classType == null) {
      throw new SimpleMessageException("classType not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      List<PermissibleObject> mostRated = new ArrayList<PermissibleObject>();
      String simpleClassName = Class.forName(classType).getSimpleName();
      List<PermissibleObject> list = session.get()
          .createQuery("from " + simpleClassName + " where creationDate >= " + sinceDateMillis + " order by creationDate desc").setMaxResults(maxResults)
          .setCacheable(true).list();
      for (PermissibleObject permissibleObject : list) {
        if (SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
          mostRated.add(permissibleObject);
        }
      }
      return mostRated;
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  private String getVoterGUID() {
    Cookie cookies[] = getThreadLocalRequest().getCookies();
    String voterGUID = UUID.randomUUID().toString();
    boolean hasVoterGUID = false;
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("voterGUID".equals(cookie.getName())) {
          hasVoterGUID = true;
          voterGUID = cookie.getValue();
          break;
        }
      }
    }
    if (!hasVoterGUID) {
      Cookie voterGUIDCookie = new Cookie("voterGUID", voterGUID);
      voterGUIDCookie.setPath("/");
      voterGUIDCookie.setMaxAge(COOKIE_TIMEOUT);
      getThreadLocalResponse().addCookie(voterGUIDCookie);
    }
    return voterGUID;
  }

  public PermissibleObject getNextUnratedPermissibleObject(String objectType) throws SimpleMessageException {
    if (StringUtils.isEmpty(objectType)) {
      throw new SimpleMessageException("Type not supplied.");
    }
    PermissibleObject object = null;
    User authUser = getAuthenticatedUser(session.get());
    object = RatingHelper.getNextUnratedPermissibleObject(session.get(), objectType, authUser, getVoterGUID());
    return object;
  }

  public UserAdvisory getUserAdvisory(PermissibleObject permissibleObject) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      permissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId());

      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
        throw new SimpleMessageException("User is not authorized to get advisory on this content.");
      }
      // find rating based on remote address if needed
      return AdvisoryHelper.getUserAdvisory(session.get(), permissibleObject, authUser, getVoterGUID());
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public UserAdvisory setUserAdvisory(PermissibleObject permissibleObject, int advisory) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    Transaction tx = session.get().beginTransaction();
    try {
      permissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId());

      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
        throw new SimpleMessageException("User is not authorized to set advisory on this content.");
      }

      // check if rating already exists
      UserAdvisory userAdvisory = AdvisoryHelper.getUserAdvisory(session.get(), permissibleObject, authUser, getVoterGUID());
      if (userAdvisory != null) {
        throw new SimpleMessageException("Already voted.");
      }

      float totalAdvisory = (float) permissibleObject.getNumAdvisoryVotes() * permissibleObject.getAverageAdvisory();
      totalAdvisory += advisory;
      permissibleObject.setNumAdvisoryVotes(permissibleObject.getNumAdvisoryVotes() + 1);
      float newAvg = totalAdvisory / (float) permissibleObject.getNumAdvisoryVotes();
      permissibleObject.setAverageAdvisory(newAvg);
      session.get().save(permissibleObject);

      userAdvisory = new UserAdvisory();
      userAdvisory.setPermissibleObject(permissibleObject);
      userAdvisory.setRating(advisory);
      userAdvisory.setVoter(authUser);
      userAdvisory.setVoterGUID(getVoterGUID());

      session.get().save(userAdvisory);
      tx.commit();
      return userAdvisory;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public Boolean submitComment(Comment comment) throws SimpleMessageException {
    if (comment == null) {
      throw new SimpleMessageException("Comment not supplied.");
    }
    if (comment.getParent() == null) {
      throw new SimpleMessageException("PermissibleObject not supplied with comment.");
    }
    User authUser = getAuthenticatedUser(session.get());
    Transaction tx = session.get().beginTransaction();
    try {
      PermissibleObject parentPermissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, comment.getParent().getId());
      parentPermissibleObject.setNumComments(parentPermissibleObject.getNumComments() + 1);
      comment.setParent(parentPermissibleObject);
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, comment.getParent(), PERM.READ)) {
        throw new SimpleMessageException("User is not authorized to make comments on this content.");
      }
      if (!comment.getParent().isAllowComments()) {
        throw new SimpleMessageException("Comments are not allowed on this content.");
      }
      // the comment is approved if we are not moderating or if the commenter is the file owner
      comment.setApproved(!comment.getParent().isModerateComments() || comment.getParent().getOwner().equals(authUser));
      comment.setAuthorIP(getThreadLocalRequest().getRemoteAddr());
      session.get().save(comment);
      session.get().save(parentPermissibleObject);
      tx.commit();
      return true;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public Boolean approveComment(Comment comment) throws SimpleMessageException {
    if (comment == null) {
      throw new SimpleMessageException("Comment not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException(".");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      comment = ((Comment) session.get().load(Comment.class, comment.getId()));
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, comment.getParent(), PERM.WRITE)) {
        throw new SimpleMessageException("User is not authorized to approve comments for this content.");
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
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public Boolean deleteComment(Comment comment) throws SimpleMessageException {
    if (comment == null) {
      throw new SimpleMessageException("Comment not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      comment = ((Comment) session.get().load(Comment.class, comment.getId()));
      boolean isAuthor = comment.getAuthor() != null && comment.getAuthor().equals(authUser);
      if (!isAuthor && !SecurityHelper.doesUserHavePermission(session.get(), authUser, comment.getParent(), PERM.WRITE)) {
        throw new SimpleMessageException("User is not authorized to delete comments for this content.");
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
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public PermissibleObject getPermissibleObject(Long id) throws SimpleMessageException {
    if (id == null) {
      throw new SimpleMessageException("Id not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    try {
      PermissibleObject permissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, id);
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
        throw new SimpleMessageException("User is not authorized to get this content.");
      }
      return permissibleObject;
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public RepositoryTreeNode getRepositoryTree() throws SimpleMessageException {
    try {
      User authUser = getAuthenticatedUser(session.get());
      RepositoryTreeNode root = new RepositoryTreeNode();
      RepositoryHelper.buildRepositoryTreeNode(session.get(), authUser, root, null);
      return root;
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public PermissibleObject savePermissibleObject(PermissibleObject permissibleObject) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("Object not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null && permissibleObject instanceof IAnonymousPermissibleObject) {
      authUser = UserHelper.getUser(session.get(), "anonymous");
    }
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }

    Transaction tx = session.get().beginTransaction();
    try {
      if (permissibleObject.getParent() != null) {
        permissibleObject.setParent((PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getParent().getId()));
      }
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject.getParent(), PERM.CREATE_CHILD)) {
        throw new SimpleMessageException("User is not authorized to write to parent folder.");
      }
      if (permissibleObject.getId() != null) {
        PermissibleObject hibNewObject = (PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId());
        if (hibNewObject != null) {
          if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, hibNewObject, PERM.WRITE)) {
            throw new SimpleMessageException("User is not authorized to overwrite object.");
          }
          List<Field> fields = ReflectionCache.getFields(permissibleObject.getClass());
          for (Field field : fields) {
            try {
              field.set(hibNewObject, field.get(permissibleObject));
            } catch (Exception e) {
              e.printStackTrace();
              Logger.log(e);
            }
          }

          permissibleObject = hibNewObject;
        }
      }

      List<Field> fields = ReflectionCache.getFields(permissibleObject.getClass());
      for (Field field : fields) {
        try {
          // do not update parent permission only our 'owned' objects
          if (!"parent".equals(field.getName())) {
            Object obj = field.get(permissibleObject);
            if (obj instanceof PermissibleObject) {
              PermissibleObject childObj = (PermissibleObject) obj;
              PermissibleObject hibChild = (PermissibleObject) session.get().load(PermissibleObject.class, childObj.getId());
              hibChild.setGlobalRead(permissibleObject.isGlobalRead());
              hibChild.setGlobalWrite(permissibleObject.isGlobalWrite());
              hibChild.setGlobalExecute(permissibleObject.isGlobalExecute());
              hibChild.setGlobalCreateChild(permissibleObject.isGlobalCreateChild());
              field.set(permissibleObject, hibChild);
            }
          }
        } catch (Exception e) {
          Logger.log(e);
        }
      }

      permissibleObject.setOwner(authUser);
      session.get().save(permissibleObject);
      tx.commit();
      return permissibleObject;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<PermissibleObject> savePermissibleObjects(List<PermissibleObject> permissibleObjects) {
    for (PermissibleObject object : permissibleObjects) {
      savePermissibleObject(object);
    }
    return permissibleObjects;
  }

  public void deletePermissibleObject(PermissibleObject permissibleObject) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("Object not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();

    permissibleObject = ((PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId()));

    try {
      if (permissibleObject instanceof Folder) {
        Folder folder = (Folder) permissibleObject;
        if (!authUser.isAdministrator() && !authUser.equals(folder.getOwner())) {
          throw new SimpleMessageException("User is not authorized to delete this object.");
        }
        FolderHelper.deleteFolder(session.get(), folder);
      } else {
        if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.WRITE)) {
          throw new SimpleMessageException("User is not authorized to delete this object.");
        }
        // just try to delete the object, hopefully it has no children
        PermissibleObjectHelper.deletePermissibleObject(session.get(), permissibleObject);
      }
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public void deletePermissibleObjects(Set<PermissibleObject> permissibleObjects) throws SimpleMessageException {
    if (permissibleObjects == null) {
      throw new SimpleMessageException("Objects not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    for (PermissibleObject permissibleObject : permissibleObjects) {
      deletePermissibleObject(permissibleObject);
    }
  }

  public List<PermissibleObject> getMyPermissibleObjects(PermissibleObject parent, String objectType) throws SimpleMessageException {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Class<?> clazz;
    try {
      clazz = Class.forName(objectType);
      return PermissibleObjectHelper.getMyPermissibleObjects(session.get(), authUser, parent, clazz);
    } catch (ClassNotFoundException cnfe) {
      throw new SimpleMessageException(cnfe.getMessage());
    }
  }

  public List<PermissibleObject> getPermissibleObjects(PermissibleObject parent, String objectType) throws SimpleMessageException {
    try {
      User authUser = getAuthenticatedUser(session.get());
      ArrayList<PermissibleObject> objects = new ArrayList<PermissibleObject>();
      Class<?> clazz = Class.forName(objectType);
      RepositoryHelper.getPermissibleObjects(session.get(), authUser, objects, parent, clazz);
      return objects;
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public PermissibleObjectTreeNode getPermissibleObjectTree(PermissibleObject parent, User owner, List<String> acceptedClasses, int fetchDepth,
      int metaDataFetchDepth) throws SimpleMessageException {
    try {
      User authUser = getAuthenticatedUser(session.get());
      PermissibleObjectTreeNode root = new PermissibleObjectTreeNode();
      if (parent != null) {
        parent = getPermissibleObject(parent.getId());
      }
      RepositoryHelper.buildPermissibleObjectTreeNode(session.get(), authUser, owner, getVoterGUID(), root, parent, acceptedClasses, 0, fetchDepth,
          metaDataFetchDepth);
      return root;
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public Folder createNewFolder(Folder newFolder) throws SimpleMessageException {
    if (newFolder == null) {
      throw new SimpleMessageException("Folder not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      if (newFolder.getParent() != null) {
        newFolder.setParent((PermissibleObject) session.get().load(PermissibleObject.class, newFolder.getParent().getId()));
      }
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, newFolder.getParent(), PERM.WRITE)) {
        throw new SimpleMessageException("User is not authorized to create a new folder here.");
      }
      if (newFolder.getId() != null) {
        Folder hibNewFolder = (Folder) session.get().load(Folder.class, newFolder.getId());
        if (hibNewFolder != null) {
          if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, hibNewFolder, PERM.WRITE)) {
            throw new SimpleMessageException("User is not authorized to save a new folder here.");
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
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public void renameFile(File file) throws SimpleMessageException {
    if (file == null) {
      throw new SimpleMessageException("File not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      File hibfile = (File) session.get().load(File.class, file.getId());
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, hibfile, PERM.WRITE)) {
        throw new SimpleMessageException("User is not authorized to rename this file.");
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
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public void renameFolder(Folder folder) throws SimpleMessageException {
    if (folder == null) {
      throw new SimpleMessageException("Folder not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      Folder hibfolder = (Folder) session.get().load(Folder.class, folder.getId());
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, hibfolder, PERM.WRITE)) {
        throw new SimpleMessageException("User is not authorized to rename this folder.");
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
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<Permission> getPermissions(PermissibleObject permissibleObject) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    try {
      permissibleObject = ((PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId()));
      if (!authUser.isAdministrator() && !permissibleObject.getOwner().equals(authUser)) {
        throw new SimpleMessageException("User is not authorized to get permissions on this content.");
      }
      return SecurityHelper.getPermissions(session.get(), permissibleObject);
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public PermissibleObject updatePermissibleObject(PermissibleObject permissibleObject) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      User newOwner = ((User) session.get().load(User.class, permissibleObject.getOwner().getId()));
      PermissibleObject hibPermissibleObject = ((PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId()));
      if (!authUser.isAdministrator() && !hibPermissibleObject.getOwner().equals(authUser)) {
        throw new SimpleMessageException("User is not authorized to update this object.");
      }
      // update fields (for example, image has child permissibles)
      List<Field> fields = ReflectionCache.getFields(hibPermissibleObject.getClass());
      for (Field field : fields) {
        try {
          if (!field.getName().equals("parent") && PermissibleObject.class.isAssignableFrom(field.getType())) {
            Object obj = field.get(hibPermissibleObject);
            if (obj == null) {
              field.set(hibPermissibleObject, field.get(permissibleObject));
              obj = field.get(hibPermissibleObject);
              if (obj != null) {
                PermissibleObject hibSubObj = ((PermissibleObject) session.get().load(PermissibleObject.class, ((PermissibleObject) obj).getId()));
                obj = hibSubObj;
              }
            }
            if (obj != null) {
              PermissibleObject childObj = (PermissibleObject) obj;
              childObj.setGlobalRead(hibPermissibleObject.isGlobalRead());
              childObj.setGlobalWrite(hibPermissibleObject.isGlobalWrite());
              childObj.setGlobalExecute(hibPermissibleObject.isGlobalExecute());
              childObj.setGlobalCreateChild(hibPermissibleObject.isGlobalCreateChild());
              session.get().save(childObj);
            }
          }
          if (!field.getName().equals("parent")) {
            try {
              field.set(hibPermissibleObject, field.get(permissibleObject));
            } catch (Throwable t) {
            }
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
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<PermissibleObject> updatePermissibleObjects(List<PermissibleObject> permissibleObjects) throws SimpleMessageException {
    for (PermissibleObject object : permissibleObjects) {
      updatePermissibleObject(object);
    }
    return permissibleObjects;
  }

  public void setPermissions(PermissibleObject permissibleObject, List<Permission> permissions) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not supplied.");
    }
    if (permissions == null) {
      throw new SimpleMessageException("Permissions not supplied.");
    }
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      PermissibleObject hibPermissibleObject = ((PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId()));
      if (!authUser.isAdministrator() && !hibPermissibleObject.getOwner().equals(authUser)) {
        throw new SimpleMessageException("User is not authorized to set permissions on this object.");
      }
      session.get().evict(authUser);

      SecurityHelper.deletePermissions(session.get(), permissibleObject);
      for (Permission permission : permissions) {
        session.get().save(permission);
      }

      List<Field> fields = ReflectionCache.getFields(permissibleObject.getClass());
      for (Field field : fields) {
        try {
          // do not update parent permission only our 'owned' objects
          if (!"parent".equals(field.getName())) {
            Object obj = field.get(permissibleObject);
            if (obj instanceof PermissibleObject) {
              PermissibleObject childObj = (PermissibleObject) obj;
              childObj.setGlobalRead(permissibleObject.isGlobalRead());
              childObj.setGlobalWrite(permissibleObject.isGlobalWrite());
              childObj.setGlobalExecute(permissibleObject.isGlobalExecute());
              childObj.setGlobalCreateChild(permissibleObject.isGlobalCreateChild());
              SecurityHelper.deletePermissions(session.get(), childObj);
              for (Permission permission : permissions) {
                Permission newPerm = new Permission();
                newPerm.setPermissibleObject(childObj);
                newPerm.setSecurityPrincipal(permission.getSecurityPrincipal());
                newPerm.setReadPerm(permission.isReadPerm());
                newPerm.setWritePerm(permission.isWritePerm());
                newPerm.setExecutePerm(permission.isExecutePerm());
                newPerm.setCreateChildPerm(permission.isCreateChildPerm());
                session.get().save(newPerm);
              }
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
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public Long getCustomCounter1(PermissibleObject permissibleObject) {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not supplied.");
    }
    try {
      permissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId());
      return permissibleObject.getCustomCounter1();
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public Long incrementCustomCounter1(PermissibleObject permissibleObject) {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not supplied.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      permissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId());
      permissibleObject.setCustomCounter1(permissibleObject.getCustomCounter1() + 1);
      session.get().save(permissibleObject);
      tx.commit();
      return permissibleObject.getCustomCounter1();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public PermissibleObject echoPermissibleObject(PermissibleObject permissibleObject) throws SimpleMessageException {
    return permissibleObject;
  }

  public void submitReferral(Referral referral) throws SimpleMessageException {
    // Document.get().getReferrer();

    List<Referral> referrals = session.get().createQuery("from " + Referral.class.getSimpleName() + " where referralURL = '" + referral.getReferralURL() + "'")
        .setMaxResults(1).setCacheable(true).list();
    if (referrals.size() > 0) {
      referral = referrals.get(0);
      referral.counter++;
    } else {
      referral.counter = 1L;
    }
    referral.recentDate = System.currentTimeMillis();

    if (StringUtils.isEmpty(referral.getReferralURL())) {
      return;
    }

    Transaction tx = session.get().beginTransaction();
    try {
      session.get().save(referral);
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }

  }

  public List<Referral> getReferrals(PermissibleObject subject) throws SimpleMessageException {
    List<Referral> referrals = null;
    if (subject == null) {
      referrals = session.get().createQuery("from " + Referral.class.getSimpleName()).setCacheable(true).list();
    } else {
      referrals = session.get().createQuery("from " + Referral.class.getSimpleName() + " where subject.id = " + subject.getId()).setCacheable(true).list();
    }
    return referrals;
  }

  public Page<PermissibleObject> getPage(PermissibleObject parent, String pageClassType, String sortField, boolean sortDescending, int pageNumber, int pageSize)
      throws SimpleMessageException {
    User authUser = getAuthenticatedUser(session.get());
    try {
      Class<?> clazz = Class.forName(pageClassType);
      return PageHelper.getPage(session.get(), parent, clazz, authUser, sortField, sortDescending, pageNumber, pageSize);
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public PageInfo getPageInfo(PermissibleObject parent, String pageClassType, int pageSize) throws SimpleMessageException {
    try {
      User authUser = getAuthenticatedUser(session.get());
      Class<?> clazz = Class.forName(pageClassType);
      return PageHelper.getPageInfo(session.get(), parent, clazz, authUser, pageSize);
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public FileUploadStatus getFileUploadStatus() throws SimpleMessageException {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    try {
      FileUploadStatus status = fileUploadStatusMap.get(authUser);
      if (status == null) {
        throw new SimpleMessageException("No stats currently available.");
      }
      return status;
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<PermissibleObjectTreeNode> searchPermissibleObjects(PermissibleObject parent, String query, String sortField, boolean sortDescending,
      String searchObjectType, boolean searchNames, boolean searchDescriptions, boolean searchKeywords, boolean useExactPhrase) throws SimpleMessageException {
    User user = getAuthenticatedUser(session.get());
    // return all permissible objects which match the name/description
    try {
      Class<?> clazz = Class.forName(searchObjectType);
      return PermissibleObjectHelper.search(session.get(), user, getVoterGUID(), clazz, query, sortField, sortDescending, searchNames, searchDescriptions,
          searchKeywords, useExactPhrase);
    } catch (Throwable t) {
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<PermissibleObject> getTaggedPermissibleObjects(Tag tag) throws SimpleMessageException {
    // TODO Auto-generated method stub
    return null;
  }

  public void createTag(String tagName, String tagDescription, Tag parentTag) throws SimpleMessageException {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }

    if (StringUtils.isEmpty(tagName)) {
      throw new SimpleMessageException("Tag name not provided.");
    }

    Tag hibParentTag = null;
    if (parentTag != null) {
      hibParentTag = ((Tag) session.get().load(Tag.class, parentTag.getId()));
    }

    Transaction tx = session.get().beginTransaction();
    try {
      Tag tag = new Tag();
      tag.setName(tagName);
      tag.setDescription(tagDescription);
      tag.setParentTag(hibParentTag);
      session.get().save(tag);
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public void deleteTag(final Tag tag) throws SimpleMessageException {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }

    if (tag == null) {
      throw new SimpleMessageException("Tag not provided.");
    }

    Tag hibTag = ((Tag) session.get().load(Tag.class, tag.getId()));
    if (hibTag == null) {
      throw new SimpleMessageException("Tag not found: " + tag);
    }

    Transaction tx = session.get().beginTransaction();
    try {
      TagHelper.deleteTag(session.get(), hibTag);
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<Tag> getTags() throws SimpleMessageException {
    // anyone can get tags
    return TagHelper.getTags(session.get());
  }

  public void addToTag(final Tag tag, final PermissibleObject permissibleObject) throws SimpleMessageException {
    TagMembership tagMembership = new TagMembership();
    tagMembership.setTag(tag);
    tagMembership.setPermissibleObject(permissibleObject);
    addToTag(tagMembership);
  }

  public void addToTag(final TagMembership tagMembership) throws SimpleMessageException {
    if (tagMembership == null) {
      throw new SimpleMessageException("TagMembership not provided.");
    }

    if (tagMembership.getTag() == null) {
      throw new SimpleMessageException("Tag not provided.");
    }

    if (tagMembership.getPermissibleObject() == null) {
      throw new SimpleMessageException("PermissibleObject not provided.");
    }

    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }

    // assumption is that the membership does not exist but the category / permissible object do
    // they must be loaded
    Tag hibTag = ((Tag) session.get().load(Tag.class, tagMembership.getTag().getId()));
    if (hibTag == null) {
      throw new SimpleMessageException("Tag not found: " + tagMembership.getTag().getId());
    }

    PermissibleObject hibPermissibleObject = ((PermissibleObject) session.get().load(PermissibleObject.class, tagMembership.getPermissibleObject().getId()));
    if (hibPermissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not found: " + tagMembership.getPermissibleObject());
    }

    Transaction tx = session.get().beginTransaction();
    try {
      tagMembership.setTag(hibTag);
      tagMembership.setPermissibleObject(hibPermissibleObject);
      session.get().save(tagMembership);
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<Tag> getTags(PermissibleObject permissibleObject) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not provided.");
    }
    return TagHelper.getTags(session.get(), permissibleObject);
  }

  public void removeTagMembership(final TagMembership tagMembership) throws SimpleMessageException {
    removeFromTag(tagMembership.getTag(), tagMembership.getPermissibleObject());
  }

  public void removeFromTag(final Tag tag, final PermissibleObject permissibleObject) throws SimpleMessageException {
    User authUser = getAuthenticatedUser(session.get());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }

    if (tag == null) {
      throw new SimpleMessageException("Tag not provided.");
    }

    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not provided.");
    }

    Tag hibTag = ((Tag) session.get().load(Tag.class, tag.getId()));
    if (hibTag == null) {
      throw new SimpleMessageException("Category not found: " + tag);
    }

    PermissibleObject hibPermissibleObject = ((PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId()));
    if (hibPermissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not found: " + permissibleObject);
    }

    Transaction tx = session.get().beginTransaction();
    try {
      TagMembership cm = TagHelper.getTagMembership(session.get(), hibTag, permissibleObject);
      session.get().delete(cm);
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public void sendEmail(PermissibleObject permissibleObject, final String subject, final String message, String fromAddress, String fromName, String toAddresses) {
    User user = null;
    try {
      user = getAuthenticatedUser();
    } catch (Throwable t) {
    }
    if (user != null) {
      fromName = user.getFirstname();
      fromAddress = user.getEmail();
    }
    StringTokenizer st = new StringTokenizer(toAddresses, ";");
    while (st.hasMoreTokens()) {
      String toAddress = st.nextToken();
      String toName = st.nextToken();

      // replace {toAddress} with toAddress on server
      // replace {toName} with toName on server
      String tmpSubject = subject;
      tmpSubject = tmpSubject.replace("{toAddress}", toAddress); //$NON-NLS-1$ 
      tmpSubject = tmpSubject.replace("{toName}", toName); //$NON-NLS-1$ 

      String tmpMessage = message;
      tmpMessage = tmpMessage.replace("{toAddress}", toAddress); //$NON-NLS-1$ 
      tmpMessage = tmpMessage.replace("{toName}", toName); //$NON-NLS-1$ 

      BaseSystem.getEmailService().sendMessage(BaseSystem.getSmtpHost(), fromAddress, fromName, toAddress, tmpSubject, tmpMessage);
    }
  }

  public Boolean submitAdvertisingInfo(String contactName, String email, String company, String phone, String comments) throws SimpleMessageException {
    String text = "Contact Name: " + contactName + "<BR>";
    text += "E-Mail: " + email + "<BR>";
    text += "Company: " + company + "<BR>";
    text += "Phone: " + phone + "<BR>";
    text += "Comments: " + comments + "<BR>";
    return BaseSystem.getEmailService().sendMessage(BaseSystem.getSmtpHost(), BaseSystem.getAdminEmailAddress(), contactName,
        BaseSystem.getAdminEmailAddress(), contactName + " is interested in advertising on " + BaseSystem.getDomainName(), text);
  }

  public Boolean submitFeedback(String contactName, String email, String phone, String comments) throws SimpleMessageException {
    String text = "Contact Name: " + contactName + "<BR>";
    text += "E-Mail: " + email + "<BR>";
    text += "Phone: " + phone + "<BR>";
    text += "Comments: " + comments + "<BR>";
    return BaseSystem.getEmailService().sendMessage(BaseSystem.getSmtpHost(), BaseSystem.getAdminEmailAddress(), contactName,
        BaseSystem.getAdminEmailAddress(), contactName + " has submitted feedback for " + BaseSystem.getDomainName(), text);
  }

  public User submitAccountValidation(String username, String validationCode) throws SimpleMessageException {
    Transaction tx = session.get().beginTransaction();
    try {
      User user = UserHelper.getUser(session.get(), username);
      if (user != null && !user.isValidated()) {
        MD5 md5 = new MD5();
        md5.Update(user.getEmail());
        md5.Update(user.getPasswordHash());
        if (validationCode.equals(md5.asHex())) {
          // validation successful
          user.setValidated(true);
          login(session.get(), getThreadLocalRequest(), getThreadLocalResponse(), username, user.getPasswordHash(), true);
          tx.commit();
        } else {
          throw new SimpleMessageException("Account could not be activated, validation code does not match our records.");
        }
      } else {
        throw new SimpleMessageException("Account does not exist or is already validated.");
      }
      return user;
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    } finally {
      try {
        tx.rollback();
      } catch (Throwable t) {
      }
    }
  }

}
