package org.damour.base.server.hibernate;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileUploadTest {

  @Test
  public void uploadTest() {
    try {

      System.out.println((new File("")).getAbsolutePath());
      
      PostMethod filePost = new PostMethod("http://test.sometests.com/test.php");
      ArrayList<Part> parts = new ArrayList<Part>();
      parts.add(new FilePart("blah.txt", new ByteArrayPartSource("blahsource.txt", "contentdata".getBytes())));// NON-NLS
      parts.add(new FilePart("blah.jpg", new File("blah.txt"), "image/shit", "UTF8"));// NON-NLS
      // parts.add(new FilePart(imageName, new ByteArrayPartSource(imageName, getImageData("/res/icons/" + imageName))));// NON-NLS
      filePost.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[parts.size()]), filePost.getParams()));
      HttpClient client = new HttpClient();
      int status = client.executeMethod(filePost);

      System.out.println("POST Status: " + status);
      String postResult = filePost.getResponseBodyAsString();
      System.out.println(postResult);
      
    } catch (Throwable t) {
      System.out.println("exception thrown");
      StringWriter sw = new StringWriter();
      PrintWriter outWriter = new PrintWriter(sw);
      t.printStackTrace(outWriter);
      String trace = sw.toString().replaceAll("\n", "<BR/>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
      System.out.println(trace);
    }
  }

  @Before
  public void before() {
  }

  @After
  public void after() {
  }

}
