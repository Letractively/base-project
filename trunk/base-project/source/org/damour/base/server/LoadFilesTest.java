package org.damour.base.server;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.damour.base.client.objects.File;
import org.damour.base.server.hibernate.HibernateUtil;
import org.hibernate.Session;

public class LoadFilesTest extends HttpServlet {

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Session session = HibernateUtil.getInstance().getSession();
    List<File> files = session.createQuery("from File").list();
    for (File file : files) {
      System.out.println("file name: " + file.getName());
      System.out.println("file desc: " + file.getDescription());
      System.out.println("file type: " + file.getContentType());
      System.out.println("file size: " + file.getSize());
    }
    session.close();

  }
}
