package org.damour.base.client.ui.repository;


public interface IFileUploadCallback {
  public void fileUploaded(String id);
  public void uploadFailed();
}
