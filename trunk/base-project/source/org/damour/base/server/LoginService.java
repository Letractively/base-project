package org.damour.base.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.damour.base.client.objects.User;

public class LoginService extends HttpServlet {

  private static BaseServiceImpl baseService = new BaseServiceImpl();

  public LoginService() {
    super();
  }

  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    try {
      User user = baseService.login(request, response, request.getParameter("username"), request.getParameter("password"));
      response.setContentType("text/plain");
      response.getOutputStream().write("true".getBytes());
      return;
    } catch (Throwable t) {
    }
    response.getOutputStream().write("false".getBytes());
  }
}
