package org.damour.base.server.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.damour.base.server.hibernate.helpers.UserHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;

import com.twmacinta.util.MD5;

public class SecurityHelperTest {

  @Test
  public void createUserTest() {
    String username = "testuser";
    User user = new User();
    user.setUsername(username);
    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();
    session.save(user);
    tx.commit();
    session.close();

    session = HibernateUtil.getInstance().getSession();
    user = UserHelper.getUser(session, username);
    session.close();
    assertEquals(username, user.getUsername());
  }

  @Test
  public void createDuplicateUserTest() {
    try {
      String username = "testuser";
      User user1 = new User();
      user1.setUsername(username);
      User user2 = new User();
      user2.setUsername(username);
      Session session = HibernateUtil.getInstance().getSession();
      Transaction tx = session.beginTransaction();
      session.save(user1);
      session.save(user2);
      tx.commit();
      session.close();
      throw new AssertionError("Created duplicate users!");
    } catch (ConstraintViolationException e) {
      // perfect!
    }
  }

  @Test
  public void getUsersTest() {
    // create some users
    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();
    int NUM_USERS = 100;
    for (int i = 0; i < NUM_USERS; i++) {
      User user = new User();
      user.setUsername("username" + i);
      session.save(user);
    }
    tx.commit();
    session.close();

    session = HibernateUtil.getInstance().getSession();
    List<User> users = SecurityHelper.getUsers(session);
    assertNotNull(users);
    assertEquals(users.size(), NUM_USERS);
    session.close();
  }

  @Test
  public void getUsernamesTest() {
    // create some users
    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();
    int NUM_USERS = 100;
    for (int i = 0; i < NUM_USERS; i++) {
      User user = new User();
      user.setUsername("username" + i);
      session.save(user);
    }
    tx.commit();
    session.close();

    session = HibernateUtil.getInstance().getSession();
    List<String> users = SecurityHelper.getUsernames(session);
    assertNotNull(users);
    assertEquals(users.size(), NUM_USERS);
    session.close();

    HibernateUtil.getInstance().printStatistics();
  }

  @Test
  public void getGroupMembershipsTest() {
    // create some users and groups
    // create some users
    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();
    int NUM_USERS = 100;
    for (int i = 0; i < NUM_USERS; i++) {
      User user = new User();
      user.setUsername("username" + i);
      session.save(user);
    }
    int NUM_GROUPS = 100;
    List<User> users = SecurityHelper.getUsers(session);
    for (int i = 0; i < NUM_GROUPS; i++) {
      UserGroup group = new UserGroup();
      group.setName("group" + i);
      session.save(group);
      for (User user : users) {
        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setUser(user);
        groupMembership.setUserGroup(group);
        session.save(groupMembership);
      }
    }
    tx.commit();
    session.close();

    session = HibernateUtil.getInstance().getSession();
    users = SecurityHelper.getUsers(session);
    for (User user : users) {
      List<GroupMembership> groupMemberships = SecurityHelper.getGroupMemberships(session, user);
      assertNotNull(groupMemberships);
      assertEquals(groupMemberships.size(), NUM_GROUPS);
    }
    session.close();
    HibernateUtil.getInstance().printStatistics();
  }

  @Test
  public void getUsersInUserGroupTest() {
    // create some users and groups
    // create some users
    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();
    int NUM_USERS = 10;
    for (int i = 0; i < NUM_USERS; i++) {
      User user = new User();
      user.setUsername("username" + i);
      session.save(user);
    }
    int NUM_GROUPS = 10;
    List<User> users = SecurityHelper.getUsers(session);
    for (int i = 0; i < NUM_GROUPS; i++) {
      UserGroup group = new UserGroup();
      group.setName("group" + i);
      session.save(group);
      for (User user : users) {
        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setUser(user);
        groupMembership.setUserGroup(group);
        session.save(groupMembership);
      }
    }
    tx.commit();
    session.close();

    session = HibernateUtil.getInstance().getSession();
    List<UserGroup> groups = SecurityHelper.getUserGroups(session);
    for (UserGroup group : groups) {
      users = SecurityHelper.getUsersInUserGroup(session, group);
      assertNotNull(users);
      assertEquals(users.size(), NUM_GROUPS);
    }
    session.close();
    HibernateUtil.getInstance().printStatistics();
  }

  @Test
  public void getUserGroupForUserTest() {
    // create some users and groups
    // create some users
    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();
    int NUM_USERS = 10;
    for (int i = 0; i < NUM_USERS; i++) {
      User user = new User();
      user.setUsername("username" + i);
      session.save(user);
    }
    int NUM_GROUPS = 10;
    List<User> users = SecurityHelper.getUsers(session);
    for (int i = 0; i < NUM_GROUPS; i++) {
      UserGroup group = new UserGroup();
      group.setName("group" + i);
      session.save(group);
      for (User user : users) {
        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setUser(user);
        groupMembership.setUserGroup(group);
        session.save(groupMembership);
      }
    }
    tx.commit();
    session.close();

    session = HibernateUtil.getInstance().getSession();
    for (int i = 0; i < 100; i++) {
      for (User user : users) {
        List<UserGroup> groups = SecurityHelper.getUserGroups(session, user);
        assertNotNull(groups);
        assertEquals(groups.size(), NUM_GROUPS);
      }
    }
    session.close();

    HibernateUtil.getInstance().printStatistics();
  }

  @Test
  public void createDuplicateUserInGroupTest() {
    try {
      Session session = HibernateUtil.getInstance().getSession();
      Transaction tx = session.beginTransaction();
      User user = new User();
      user.setUsername("username");
      session.save(user);
      UserGroup group = new UserGroup();
      group.setName("group");
      session.save(group);
      tx.commit();
      session.close();
      for (int i = 0; i < 2; i++) {
        session = HibernateUtil.getInstance().getSession();
        tx = session.beginTransaction();
        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setUser(user);
        groupMembership.setUserGroup(group);
        session.save(groupMembership);
        tx.commit();
        session.close();
      }
      throw new AssertionError("Created duplicate users!");
    } catch (ConstraintViolationException e) {
      // perfect!
      e.printStackTrace();
    }
  }

  @Test
  public void createDuplicateGroupTest() {
    try {
      Session session = HibernateUtil.getInstance().getSession();
      Transaction tx = session.beginTransaction();
      UserGroup group1 = new UserGroup();
      group1.setName("group");
      session.save(group1);
      UserGroup group2 = new UserGroup();
      group2.setName("group");
      session.save(group2);
      tx.commit();
      session.close();
      throw new AssertionError("Created duplicate users!");
    } catch (ConstraintViolationException e) {
      // perfect!
      e.printStackTrace();
    }
  }

  @Test
  public void removeUserFromUserGroupTest() {
    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();
    String username = "testuser";
    User user = new User();
    user.setUsername(username);
    session.save(user);

    UserGroup group = new UserGroup();
    group.setName("group");
    session.save(group);

    GroupMembership membership = new GroupMembership();
    membership.setUser(user);
    membership.setUserGroup(group);
    session.save(membership);

    tx.commit();
    session.close();

    session = HibernateUtil.getInstance().getSession();
    tx = session.beginTransaction();
    membership = SecurityHelper.getGroupMembership(session, user, group);
    session.delete(membership);
    tx.commit();
    session.close();
    assertNotNull(membership);

    session = HibernateUtil.getInstance().getSession();
    membership = SecurityHelper.getGroupMembership(session, user, group);
    session.close();
    assertNull(membership);
  }

  @Test
  public void removeUserGroupTest() {
    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();
    String username = "testuser";
    User user = new User();
    user.setUsername(username);
    session.save(user);

    UserGroup group = new UserGroup();
    group.setName("group");
    session.save(group);

    GroupMembership membership = new GroupMembership();
    membership.setUser(user);
    membership.setUserGroup(group);
    session.save(membership);

    tx.commit();
    session.close();

    session = HibernateUtil.getInstance().getSession();
    tx = session.beginTransaction();
    group = SecurityHelper.getUserGroup(session, "group");
    SecurityHelper.deleteUserGroup(session, group);
    tx.commit();
    session.close();

    session = HibernateUtil.getInstance().getSession();
    membership = SecurityHelper.getGroupMembership(session, user, group);
    session.close();
    assertNull(membership);
  }

  @Test
  public void createFolderTest() {
    String name = "test-folder-name";
    String descrption = "test-folder-description";
    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();
    Folder folder = new Folder();
    folder.setName(name);
    folder.setDescription(descrption);
    session.save(folder);
    tx.commit();
    session.close();

    session = HibernateUtil.getInstance().getSession();
    folder = SecurityHelper.getFolder(session, null, name);
    session.close();
    assertEquals(folder.getName(), name);
    assertEquals(folder.getDescription(), descrption);
  }

  @Test
  public void createSubFolderTest() {
    String parentName = "parent-folder-name";
    String parentDescription = "parent-folder-description";
    String childFolderName = "child-folder-name";
    String childFolderDescription = "child-folder-description";

    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();
    Folder parentFolder = new Folder();
    parentFolder.setName(parentName);
    parentFolder.setDescription(parentDescription);
    session.save(parentFolder);
    Folder childFolder = new Folder();
    childFolder.setParent(parentFolder);
    childFolder.setName(childFolderName);
    childFolder.setDescription(childFolderDescription);
    session.save(childFolder);
    tx.commit();
    session.close();

    session = HibernateUtil.getInstance().getSession();
    childFolder = SecurityHelper.getFolder(session, parentFolder, childFolderName);
    session.close();
    assertEquals(childFolder.getName(), childFolderName);
    assertEquals(childFolder.getDescription(), childFolderDescription);
  }

  @Test
  public void deleteFolderTest() {
    String name = "test-folder-name";
    String descrption = "test-folder-description";
    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();
    Folder folder = new Folder();
    folder.setName(name);
    folder.setDescription(descrption);
    session.save(folder);
    tx.commit();
    session.close();

    session = HibernateUtil.getInstance().getSession();
    tx = session.beginTransaction();
    session.delete(folder);
    tx.commit();
    folder = SecurityHelper.getFolder(session, null, name);
    session.close();
    assertNull(folder);
  }

  @Test
  public void passwordHashTest() {
    String password = "t@k30ff";
    MD5 md5 = new MD5();
    md5.Update(password);
    String hash1 = md5.asHex();
    md5 = new MD5();
    md5.Update(password);
    String hash2 = md5.asHex();
    assertEquals(hash1, hash2);
    System.out.println(hash1);
  }

  public void after() {
    System.out.println("*********** after begin ***********");
    HibernateUtil.getInstance().getSessionFactory().close();
    HibernateUtil.getInstance().resetHibernate();
    System.out.println("*********** after end ***********");
  }

}
