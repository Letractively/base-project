package org.damour.base.server.hibernate;

import java.util.List;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.FileComment;
import org.damour.base.client.objects.FileUserAdvisory;
import org.damour.base.client.objects.FileUserRating;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.PendingGroupMembership;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.server.hibernate.helpers.GenericPage;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
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
      FileComment comment = new FileComment();
      comment.setComment("comment " + i);
      comment.setAuthor(user);
      comment.setFile(file);
      session.save(comment);
    }
    tx.commit();
    session.close();

    for (int testCount = 0; testCount < 10; testCount++) {
      session = HibernateUtil.getInstance().getSession();
      GenericPage<FileComment> pageController = new GenericPage<FileComment>(session, FileComment.class, 0, 10);
      long pageCount = pageController.getPageCount();
      for (int pageNumber = 0; pageNumber < pageCount; pageNumber++) {
        List<FileComment> comments = pageController.getList();
        for (FileComment comment : comments) {
          System.out.print(comment.getComment());
        }
        System.out.println();
        pageController = pageController.next();
      }
      session.close();
    }
  }

  @Before
  public void before() {
    System.out.println("*********** before begin ***********");
    HibernateUtil.getInstance().setShowSQL(true);
    HibernateUtil.getInstance().setHbm2ddlMode("create-drop");
    HibernateUtil.getInstance().setTablePrefix("test_");
    HibernateUtil.getInstance().setColumnPrefix("test_");
    HibernateUtil.getInstance().resetHibernate();
    HibernateUtil.getInstance().generateHibernateMapping(User.class);
    HibernateUtil.getInstance().generateHibernateMapping(UserGroup.class);
    HibernateUtil.getInstance().generateHibernateMapping(GroupMembership.class);
    HibernateUtil.getInstance().generateHibernateMapping(PendingGroupMembership.class);
    HibernateUtil.getInstance().generateHibernateMapping(File.class);
    HibernateUtil.getInstance().generateHibernateMapping(FileComment.class);
    HibernateUtil.getInstance().generateHibernateMapping(FileUserRating.class);
    HibernateUtil.getInstance().generateHibernateMapping(FileUserAdvisory.class);
    HibernateUtil.getInstance().generateHibernateMapping(Folder.class);
    HibernateUtil.getInstance().generateHibernateMapping(Permission.class);
    System.out.println("*********** before end ***********");
  }

  @After
  public void after() {
    System.out.println("*********** after begin ***********");
    HibernateUtil.getInstance().getSessionFactory().close();
    HibernateUtil.getInstance().resetHibernate();
    System.out.println("*********** after end ***********");
  }

}
