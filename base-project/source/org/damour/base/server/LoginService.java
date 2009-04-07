package org.damour.base.server;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.damour.base.client.objects.User;

public class LoginService extends HttpServlet {

  private static BaseService baseService = new BaseService();

  public LoginService() {
    super();
  }

  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/plain");
    OutputStream out = response.getOutputStream();
    try {
      User user = baseService.login(request, response, request.getParameter("username"), request.getParameter("password"));
      out.write("true".getBytes());
      return;
    } catch (Throwable t) {
      out.write("false".getBytes());
    }
    out.close();
  }
}
