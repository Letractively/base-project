package org.damour.base.client.objects;

import java.io.Serializable;

public class FileUploadStatus implements Serializable {
  public static final int UPLOADING = 1;
  public static final int CREATING_FILE = 2;
  public static final int WRITING_FILE = 3;
  public static final int WRITING_DATABASE = 4;
  public static final int FINISHED = 5;
  
  public int status = UPLOADING;
  public long bytesRead = 0;
  public long bytesWritten = 0;
  public long contentLength = 0;
  public int item = 0;

  public FileUploadStatus() {
  }

  public long getBytesRead() {
    return bytesRead;
  }

  public void setBytesRead(long bytesRead) {
    this.bytesRead = bytesRead;
  }

  public long getContentLength() {
    return contentLength;
  }

  public void setContentLength(long contentLength) {
    this.contentLength = contentLength;
  }

  public int getItem() {
    return item;
  }

  public void setItem(int item) {
    this.item = item;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public long getBytesWritten() {
    return bytesWritten;
  }

  public void setBytesWritten(long bytesWritten) {
    this.bytesWritten = bytesWritten;
  }

}
