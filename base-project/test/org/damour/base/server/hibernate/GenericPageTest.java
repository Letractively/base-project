package org.damour.base.server.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Photo;
import org.damour.base.client.objects.User;
import org.damour.base.server.hibernate.helpers.GenericPage;
import org.damour.base.server.hibernate.helpers.PageHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
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
      comment.setAuthor(user);
      comment.setPermissibleObject(file);
      session.save(comment);
    }
    tx.commit();
    session.close();

    for (int testCount = 0; testCount < 10; testCount++) {
      session = HibernateUtil.getInstance().getSession();
      GenericPage<Comment> pageController = new GenericPage<Comment>(session, Comment.class, 0, 10);
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

    List<Photo> photosList = new ArrayList<Photo>();
    for (int i = 0; i < 100; i++) {
      Photo p = new Photo();
      p.setName("photo" + i);
      p.setGlobalRead(true);
      photosList.add(p);
      session.save(p);
    }
    tx.commit();

    for (int pageNumber = 0; pageNumber < 10; pageNumber++) {
      Page<PermissibleObject> page = PageHelper.getPage(session, Photo.class, null, true, pageNumber, 10);

      List<PermissibleObject> objs = page.getResults();
      for (PermissibleObject obj : objs) {
        System.out.print(obj.getName());
      }
      System.out.println();
    }

    tx = session.beginTransaction();
    for (Photo photo : photosList) {
      session.delete(photo);
    }
    tx.commit();
    session.close();
  }

  @After
  public void after() {
    System.out.println("*********** after begin ***********");
    HibernateUtil.getInstance().getSessionFactory().close();
    HibernateUtil.getInstance().resetHibernate();
    System.out.println("*********** after end ***********");
  }

}
