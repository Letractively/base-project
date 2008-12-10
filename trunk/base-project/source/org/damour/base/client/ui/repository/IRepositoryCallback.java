package org.damour.base.client.ui.repository;

import org.damour.base.client.objects.PermissibleObject;

public interface IRepositoryCallback {
  public void repositoryLoaded();
  public void objectRenamed(PermissibleObject object);
  public void fileUploaded(String id);
  public void fileDeleted();
}
