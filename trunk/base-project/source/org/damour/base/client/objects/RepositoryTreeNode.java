package org.damour.base.client.objects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class RepositoryTreeNode implements Serializable {

  public List<File> files;
  public HashMap<Folder, RepositoryTreeNode> folders = new HashMap<Folder, RepositoryTreeNode>();

  public RepositoryTreeNode() {
  }

  public List<File> getFiles() {
    return files;
  }

  public void setFiles(List<File> files) {
    this.files = files;
  }

  public HashMap<Folder, RepositoryTreeNode> getFolders() {
    return folders;
  }

  public void setFolders(HashMap<Folder, RepositoryTreeNode> folders) {
    this.folders = folders;
  }

}
