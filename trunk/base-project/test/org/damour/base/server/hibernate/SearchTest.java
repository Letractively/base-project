package org.damour.base.server.hibernate;

import java.util.HashMap;
import java.util.List;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.server.hibernate.helpers.PermissibleObjectHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SearchTest {

  @Test
  public void searchTest() {
    Session session = HibernateUtil.getInstance().getSession();
    System.out.println(HibernateUtil.getInstance().getHbm2ddlMode());
    Transaction tx = session.beginTransaction();

    PermissibleObject obj1 = new PermissibleObject();
    obj1.setName("abc");
    session.save(obj1);

    PermissibleObject obj2 = new PermissibleObject();
    obj2.setName("xxx");
    obj2.setKeywords("keywords are awesome");
    session.save(obj2);

    PermissibleObject obj3 = new PermissibleObject();
    obj3.setName("ghi");
    obj3.setDescription("description");
    session.save(obj3);

    tx.commit();

    List<PermissibleObject> results = PermissibleObjectHelper.search(session, PermissibleObject.class, "abc xxx", true, true, true, false);
    for (PermissibleObject result : results) {
      System.out.println("Found: " + result.getName());
    }
    
    tx = session.beginTransaction();
    session.delete(obj1);
    session.delete(obj2);
    session.delete(obj3);
    tx.commit();
    

    session.close();
  }

  @BeforeClass
  public static void before() {
    System.out.println("*********** before begin ***********");
    HashMap<String, String> overrides = new HashMap<String, String>();
    overrides.put("showSQL", "true");
    overrides.put("hbm2ddlMode", "update");
    overrides.put("tablePrefix", "test_");
    HibernateUtil.resetHibernate();
    HibernateUtil.getInstance(overrides);
    System.out.println("*********** before end ***********");
  }

  @AfterClass
  public static void after() {
    System.out.println("*********** after begin ***********");
    HibernateUtil.getInstance().getSessionFactory().close();
    HibernateUtil.resetHibernate();
    System.out.println("*********** after end ***********");
  }

}
