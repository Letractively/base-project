package org.damour.base.server;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.andrewtimberlake.captcha.Captcha;

public class CaptchaImageGeneratorService extends HttpServlet {
  private static DateFormat RFC822_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Captcha captcha = new Captcha();
    request.getSession().setAttribute("captcha", captcha);
    response.addHeader("Pragma", "No-cache");
    response.addHeader("Expires", RFC822_FORMAT.format(Calendar.getInstance().getTime()));
    String resource = request.getRequestURI();
    String extension = resource.substring(resource.lastIndexOf('.') + 1).toLowerCase();
    if (extension.equals("jpg") || extension.equals("png")) {
      BufferedImage image = captcha.generateImage();
      response.setContentType("image/" + extension);
      ImageIO.write(image, extension, response.getOutputStream());
    } else {
      BufferedImage image = captcha.generateImage();
      response.setContentType("image/png");
      ImageIO.write(image, "png", response.getOutputStream());
    }
  }
}