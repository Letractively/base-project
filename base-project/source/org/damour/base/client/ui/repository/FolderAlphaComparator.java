package org.damour.base.client.ui.repository;

import java.util.Comparator;

import org.damour.base.client.objects.Folder;

public class FolderAlphaComparator implements Comparator<Folder> {

  boolean aToZ = true;

  public FolderAlphaComparator(boolean aToZ) {
    this.aToZ = aToZ;
  }

  public int compare(Folder folder1, Folder folder2) {
    if (aToZ) {
      return folder1.getName().compareTo(folder2.getName());
    }
    // z to a
    return folder2.getName().compareTo(folder1.getName());
  };
}
