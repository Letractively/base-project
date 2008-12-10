package org.damour.base.client.ui.repository;

import java.util.Comparator;

import org.damour.base.client.objects.File;

public class FileAlphaComparator implements Comparator<File> {

  boolean aToZ = true;

  public FileAlphaComparator(boolean aToZ) {
    this.aToZ = aToZ;
  }

  public int compare(File file1, File file2) {
    if (aToZ) {
      return file1.getName().compareTo(file2.getName());
    }
    // z to a
    return file2.getName().compareTo(file1.getName());
  };
}
