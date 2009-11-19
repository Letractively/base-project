package org.damour.base.server.hibernate;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.RepositoryTreeNode;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.server.hibernate.helpers.RepositoryHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Test;

public class RepositoryTest {

  @Test
  public void test() {
    Session session = HibernateUtil.getInstance().getSession();

    Transaction tx = session.beginTransaction();
    User user = new User();
    user.setUsername("mdamour1976");
    session.save(user);

    // create 100 files which have global read = true
    for (int i = 0; i < 1; i++) {
      PermissibleObject parent = new PermissibleObject();
      parent.setName("globalRead:true " + i);
      parent.setOwner(user);
      parent.setGlobalRead(true);
      session.save(parent);

      for (int k = 0; k < 5; k++) {
        PermissibleObject folder = new PermissibleObject();
        folder.setName("globalRead:true " + i);
        folder.setOwner(user);
        folder.setGlobalRead(true);
        folder.setParent(parent);
        session.save(folder);

        for (int z = 0; z < 5; z++) {
          File file = new File();
          file.setName("globalRead:true " + z);
          file.setOwner(user);
          file.setGlobalRead(true);
          file.setParent(folder);
          session.save(file);
        }
      }
    }

    User user2 = new User();
    user2.setUsername("nobody");
    session.save(user2);

    for (int j = 0; j < 1; j++) {
      File file = new File();
      file.setName("HAS PERM " + j);
      file.setOwner(user);
      session.save(file);

      Permission perm = new Permission();
      perm.readPerm = true;
      perm.setSecurityPrincipal(user2);
      perm.setPermissibleObject(file);
      session.save(perm);
    }

    for (int j = 0; j < 1; j++) {
      PermissibleObject folder = new PermissibleObject();
      folder.setName("HAS PERM " + j);
      folder.setOwner(user);
      session.save(folder);

      Permission perm = new Permission();
      perm.readPerm = true;
      perm.setSecurityPrincipal(user2);
      perm.setPermissibleObject(folder);
      session.save(perm);
    }

    for (int j = 0; j < 1; j++) {
      PermissibleObject folder = new PermissibleObject();
      folder.setName("user owns, but does not have perm");
      folder.setOwner(user2);
      session.save(folder);

      Permission perm = new Permission();
      perm.setReadPerm(true);
      perm.setSecurityPrincipal(user);
      perm.setPermissibleObject(folder);
      session.save(perm);
    }    

    UserGroup group = new UserGroup();
    group.setName("blah");
    session.save(group);
    GroupMembership membership = new GroupMembership();
    membership.setUser(user2);
    membership.setUserGroup(group);
    session.save(membership);
    
    File groupPermFile = new File();
    groupPermFile.setName("not owner:  group perm file");
    groupPermFile.setOwner(user);
    session.save(groupPermFile);

    Permission perm = new Permission();
    perm.setReadPerm(true);
    perm.setSecurityPrincipal(group);
    perm.setPermissibleObject(groupPermFile);
    session.save(perm);
    
    tx.commit();
    session.close();

    for (int i = 0; i < 5; i++) {
      System.out.println("Starting dump");
      session = HibernateUtil.getInstance().getSession();
      // as user, get files i can see
      RepositoryTreeNode root = new RepositoryTreeNode();
      RepositoryHelper.buildRepositoryTreeNode(session, user2, root, null);
      RepositoryHelper.dumpTreeNode(root, 0);
      session.close();
      System.out.println("End dump");
    }

  }

  @After
  public void after() {
    System.out.println("*********** after begin ***********");
    HibernateUtil.getInstance().getSessionFactory().close();
    HibernateUtil.getInstance().resetHibernate();
    System.out.println("*********** after end ***********");
  }

}
