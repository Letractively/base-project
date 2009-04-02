<%@page import="java.io.File"%>
<%@page import="org.damour.base.server.Logger"%>
<%@page import="org.apache.commons.io.IOUtils"%>
<%@page import="java.io.FileInputStream"%>

<%
  long start = System.currentTimeMillis();
  try {
    response.setContentType("text/plain");
    String logString = null;
    if ("tail".equals(request.getParameter("mode"))) {
      // tail, by our definition, will just return the last 4k of text
      File logFile = new File(Logger.getLogName());
      FileInputStream logStream = new FileInputStream(logFile);
      if (logFile.length() > 4096) {
        long skip = logFile.length() - 4096;
        logStream.skip(skip);
      }
      logString = IOUtils.toString(logStream);
      logString = logString.substring(logString.indexOf("\n") + 1);
      logStream.close();
    } else {
      FileInputStream logStream = new FileInputStream(Logger.getLogName());
      logString = IOUtils.toString(logStream);
      logStream.close();
    }
    //logString = Logger.convertStringToHTML(logString);
    IOUtils.write(logString, out);
  } catch (Exception ex) {
    out.println(ex.getMessage());
  }
  long stop = System.currentTimeMillis();
  out.println((stop-start) + "ms");
%>
