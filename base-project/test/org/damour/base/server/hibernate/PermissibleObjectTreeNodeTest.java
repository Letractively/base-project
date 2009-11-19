package org.damour.base.server.hibernate;

import java.util.HashMap;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.User;
import org.damour.base.server.hibernate.helpers.RepositoryHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Test;

public class PermissibleObjectTreeNodeTest {

  @Test
  public void test() {
    HashMap<String, String> overrides = new HashMap<String, String>();
    overrides.put("tablePrefix", "bptest");
    overrides.put("hbm2ddlMode", "create-drop");
    Session session = HibernateUtil.getInstance(overrides).getSession();

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

      for (int k = 0; k < 4; k++) {
        PermissibleObject childLevelOne = new PermissibleObject();
        childLevelOne.setName("globalRead:true " + i);
        childLevelOne.setOwner(user);
        childLevelOne.setGlobalRead(true);
        childLevelOne.setParent(parent);
        session.save(childLevelOne);

        for (int z = 0; z < 3; z++) {
          PermissibleObject childLevelTwo = new PermissibleObject();
          childLevelTwo.setName("globalRead:true " + z);
          childLevelTwo.setOwner(user);
          childLevelTwo.setGlobalRead(true);
          childLevelTwo.setParent(childLevelOne);
          session.save(childLevelTwo);
          
          for (int zz = 0; zz < 2; zz++) {
            PermissibleObject childLevelThree = new PermissibleObject();
            childLevelThree.setName("globalRead:true " + zz);
            childLevelThree.setOwner(user);
            childLevelThree.setGlobalRead(true);
            childLevelThree.setParent(childLevelTwo);
            session.save(childLevelThree);
          }          
          
        }
      }
    }

    tx.commit();
    session.close();

    System.out.println("Starting dump");
    session = HibernateUtil.getInstance().getSession();
    // as user, get files i can see
    PermissibleObjectTreeNode root = new PermissibleObjectTreeNode();
    RepositoryHelper.buildPermissibleObjectTreeNode(session, user, root, null);
    RepositoryHelper.dumpTreeNode(root, 0);
    session.close();
    System.out.println("End dump");

  }

  @After
  public void after() {
    System.out.println("*********** after begin ***********");
    HibernateUtil.getInstance().getSessionFactory().close();
    HibernateUtil.getInstance().resetHibernate();
    System.out.println("*********** after end ***********");
  }

}
