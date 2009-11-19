package org.damour.base.server.hibernate;

import java.util.HashMap;
import java.util.List;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Tag;
import org.damour.base.client.objects.TagMembership;
import org.damour.base.server.hibernate.helpers.TagHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TagTest {

  public Tag createTag(String name, String desc, Tag parent) {
    Tag tag = new Tag();
    tag.setName(name);
    tag.setDescription(desc);
    tag.setParentTag(parent);
    return tag;
  }

  @Test
  public void createTagTest() {
    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();

    Tag cat = createTag("catName1", "desc1", null);
    Tag cat2 = createTag("catName2", "desc2", cat);

    session.save(cat);
    session.save(cat2);

    tx.commit();
    session.close();
  }

  @Test
  public void createTagMembership() {
    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();

    List<Tag> tags = TagHelper.getTags(session);
    for (Tag tag : tags) {
      TagMembership tagMem = new TagMembership();
      tagMem.setTag(tag);
      PermissibleObject obj = new PermissibleObject();
      obj.setName("tagMembership aware");
      session.save(obj);
      tagMem.setPermissibleObject(obj);
      session.save(tagMem);
      System.out.println(tag.getName());
    }

    tx.commit();
    session.close();
  }

  @Test
  public void deleteTagMembership() {
    Session session = HibernateUtil.getInstance().getSession();

    List<Tag> tags = TagHelper.getTags(session);
    for (Tag tag : tags) {
      Transaction tx = session.beginTransaction();
      TagMembership tagMem = new TagMembership();
      tagMem.setTag(tag);
      PermissibleObject obj = new PermissibleObject();
      obj.setName("tagMembership aware");
      session.save(obj);
      tagMem.setPermissibleObject(obj);
      session.save(tagMem);
      tx.commit();

      tx = session.beginTransaction();
      session.delete(tagMem);
      tx.commit();
    }

    session.close();
  }

  @Test
  public void deleteTag() {
    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = session.beginTransaction();

    Tag tag = createTag("to-be-deleted-parent", "desc1", null);
    Tag tag2 = createTag("to-be-deleted-child", "desc2", tag);

    TagMembership tagMem = new TagMembership();
    tagMem.setTag(tag);
    PermissibleObject obj = new PermissibleObject();
    obj.setName("tags for this should not exist");
    session.save(obj);
    tagMem.setPermissibleObject(obj);
    session.save(tagMem);
    session.save(tag);
    session.save(tag2);

    tx.commit();

    tx = session.beginTransaction();
    TagHelper.deleteTag(session, tag);
    tx.commit();

    session.close();

  }

  @BeforeClass
  public static void before() {
    HashMap<String, String> overrides = new HashMap<String, String>();
    overrides.put("showSQL", "true");
    overrides.put("hbm2ddlMode", "create-drop");
    overrides.put("tablePrefix", "test_");
    HibernateUtil.resetHibernate();
    HibernateUtil.getInstance(overrides);
  }

  @AfterClass
  public static void after() {
    System.out.println("*********** after begin ***********");
    HibernateUtil.getInstance().getSessionFactory().close();
    HibernateUtil.resetHibernate();
    System.out.println("*********** after end ***********");
  }

}
