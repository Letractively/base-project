package org.damour.base.server.hibernate.helpers;

import java.util.List;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Folder;
import org.hibernate.Session;

public class FolderHelper {

  public static List<Folder> getFolders(Session session, Folder parentFolder) {
    if (parentFolder == null) {
      return session.createQuery("from Folder where parentFolder is null").list();
    } else {
      return session.createQuery("from Folder where parentFolder.id = " + parentFolder.id).list();
    }
  }

  public static void deleteFolder(Session session, Folder folder) {
    // delete all files in this folder
    List<File> files = FileObjectHelper.getFiles(session, folder);
    for (File file : files) {
      FileObjectHelper.deleteFile(session, file);
    }
    // now delete subfolders -- recurse
    List<Folder> folders = FolderHelper.getFolders(session, folder);
    for (Folder subFolder : folders) {
      deleteFolder(session, subFolder);
    }
    // delete all permissions for folder
    SecurityHelper.deletePermissions(session, folder);
    // now that we've deleted everything else, delete the folder, finally
    session.delete(folder);
  }

}
