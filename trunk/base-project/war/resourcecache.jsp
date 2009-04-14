
<%@page import="org.damour.base.server.Logger"%><%@page import="org.damour.base.client.utils.MimeHelper"%>
<%
  response.setBufferSize(65536);
  ServletOutputStream outStream = response.getOutputStream();
  try {
    request.
    String filename = request.getParameter("filename");
    Logger.log(getClass().getSimpleName() + " received request: " + filename);
    //outStream.write(("filename: " + filename).getBytes());
    java.io.File file = new java.io.File(getServletContext().getRealPath(filename));
    java.io.InputStream inputStream = null;
    inputStream = new java.io.FileInputStream(file);
    response.setContentType(MimeHelper.getMimeTypeFromFileName(filename));
    response.setHeader("Content-Description", file.getName());
    response.setDateHeader("Last-Modified", file.lastModified());
    response.setDateHeader("Expires", System.currentTimeMillis() + 31536000000L);
    response.setContentLength((int) file.length());
    org.apache.commons.io.IOUtils.copy(inputStream, outStream);
  } catch (Throwable t) {
    Logger.log(t);
  } finally {
    try {
      outStream.flush();
    } catch (Throwable t) {
    }
    try {
      outStream.close();
    } catch (Throwable t) {
    }
  }
%>
