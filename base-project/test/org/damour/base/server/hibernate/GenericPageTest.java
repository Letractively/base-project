package org.damour.base.server.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PageInfo;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Photo;
import org.damour.base.client.objects.User;
import org.damour.base.server.hibernate.helpers.GenericPage;
import org.damour.base.server.hibernate.helpers.PageHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class GenericPageTest {

  @Test
  public void pageTest() {
    Session session = HibernateUtil.getInstance().getSession();

    Transaction tx = session.beginTransaction();
    User user = new User();
    user.setUsername("mdamour1976");
    session.save(user);

    File file = new File();
    file.setName("comment file");
    file.setOwner(user);
    session.save(file);

    for (int i = 0; i < 100; i++) {
      Comment comment = new Comment();
      comment.setComment("comment " + i);
      comment.setOwner(file.getOwner());
      comment.setAuthor(user);
      comment.setParent(file);
      session.save(comment);
    }
    tx.commit();
    session.close();

    for (int testCount = 0; testCount < 10; testCount++) {
      session = HibernateUtil.getInstance().getSession();
      GenericPage<Comment> pageController = new GenericPage<Comment>(session, Comment.class, null, 0, 10);
      long pageCount = pageController.getPageCount();
      for (int pageNumber = 0; pageNumber < pageCount; pageNumber++) {
        List<Comment> comments = pageController.getList();
        for (Comment comment : comments) {
          System.out.print(comment.getComment());
        }
        System.out.println();
        pageController = pageController.next();
      }
      session.close();
    }
  }

  @Test
  public void genericPageTest() {
    Session session = HibernateUtil.getInstance().getSession();

    Transaction tx = session.beginTransaction();

    Random r = new Random();
    List<Photo> photosList = new ArrayList<Photo>();
    for (int i = 0; i < 100; i++) {
      Photo p = new Photo();
      p.setName("photox" + i);
      p.setGlobalRead(true);
      p.setAverageRating(100f * r.nextFloat());
      photosList.add(p);
      session.save(p);
    }
    tx.commit();

    for (int pageNumber = 0; pageNumber < 10; pageNumber++) {
      Page<PermissibleObject> page = PageHelper.getPage(session, null, Photo.class, null, "averageRating", false, pageNumber, 10);

      List<PermissibleObject> objs = page.getResults();
      for (PermissibleObject obj : objs) {
        System.out.print(obj.getName() + ":" + obj.getAverageRating() + " ");
      }
      System.out.println();
    }

    PageInfo pageInfo = PageHelper.getPageInfo(session, null, Photo.class, null, 10);
    System.out.println("Row Count: " + pageInfo.getTotalRowCount());
    System.out.println("Last Page: " + pageInfo.getLastPageNumber());
    
    tx = session.beginTransaction();
    for (Photo photo : photosList) {
      session.delete(photo);
    }
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

  @After
  public void after() {
    System.out.println("*********** after begin ***********");
    HibernateUtil.getInstance().getSessionFactory().close();
    HibernateUtil.resetHibernate();
    System.out.println("*********** after end ***********");
  }

}
