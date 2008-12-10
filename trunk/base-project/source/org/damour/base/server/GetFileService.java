package org.damour.base.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.damour.base.client.Logger;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.User;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.hibernate.Session;

public class GetFileService extends HttpServlet {

  private static BaseServiceImpl baseService = new BaseServiceImpl();

  public GetFileService() {
    super();
  }

  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    Session session = HibernateUtil.getInstance().getSession();
    response.setBufferSize(65536);
    ServletOutputStream outStream = response.getOutputStream();
    InputStream inputStream = null;
    File file = null;
    try {
      // we should be able to pull items by path and by id
      // path is more tricky because of the HQL
      // (load each folder down the path until we hit the file)

      if (request.getParameter("file") == null || request.getParameter("file").equals("")) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.flushBuffer();
        Logger.log("file parameter not specified");
      }

      file = (File) session.load(File.class, new Long(request.getParameter("file")));
      Logger.log("Content requested=" + file.getName() + ":" + request.getParameter("file") + " Referral: " + request.getParameter("referer"));

      long ifModifiedSince = request.getDateHeader("If-Modified-Since");
      long fileDate = file.getLastModifiedDate() - (file.getLastModifiedDate() % 1000);

      if (fileDate <= ifModifiedSince) {
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        if ("true".equals(request.getParameter("download"))) {
          response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        } else {
          response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
        }
        response.setContentType(file.getContentType());
        response.setHeader("Content-Description", file.getName());
        response.setDateHeader("Last-Modified", file.getLastModifiedDate());
        // one year into the future
        response.setDateHeader("Expires", System.currentTimeMillis() + 31536000000L);
        response.setContentLength((int) file.getSize());
        response.flushBuffer();
        Logger.log("Conditional GET: " + file.getName());
        return;
      }

      User authUser = baseService.getAuthenticatedUser(session, request, response);
      if (!SecurityHelper.doesUserHavePermission(session, authUser, file, Permission.PERM.READ)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.flushBuffer();
        Logger.log("Forbidden content requested: " + request.getParameter("file"));
        return;
      }

      String contentType = file.getContentType();
      String name = file.getName();

      java.io.File inputFile = new java.io.File(getServletContext().getRealPath("/files/" + file.getNameOnDisk()));
      if (inputFile.exists()) {
        inputStream = new FileInputStream(inputFile);
        Logger.log("Pulling " + name + " from disk");
      }
      response.setContentType(contentType);
      if ("true".equals(request.getParameter("download"))) {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
      } else {
        response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
      }
      response.setHeader("Content-Description", name);
      response.setDateHeader("Last-Modified", file.getLastModifiedDate());
      response.setDateHeader("Expires", System.currentTimeMillis() + 31536000000L);
      response.setContentLength((int) file.getSize());
      IOUtils.copy(inputStream, outStream);
    } catch (Throwable t) {
      baseService.logException(t);
      try {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.flushBuffer();
      } catch (Throwable tt) {
      }
      try {
        response.reset();
        response.resetBuffer();
      } catch (Throwable tt) {
      }
    } finally {
      file = null;
      try {
        outStream.flush();
      } catch (Throwable t) {
      }
      try {
        outStream.close();
      } catch (Throwable t) {
      }
      try {
        session.close();
      } catch (Throwable t) {
      }
    }
  }
}
