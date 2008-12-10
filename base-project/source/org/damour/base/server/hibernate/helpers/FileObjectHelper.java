package org.damour.base.server.hibernate.helpers;

import java.lang.reflect.Field;
import java.util.List;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.FileComment;
import org.damour.base.client.objects.FileUserAdvisory;
import org.damour.base.client.objects.FileUserRating;
import org.damour.base.client.objects.Folder;
import org.damour.base.server.ReflectionCache;
import org.hibernate.Session;

public class FileObjectHelper {

  public static List<File> getFiles(Session session, Folder parentFolder) {
    if (parentFolder == null) {
      return session.createQuery("from File where parentFolder is null").list();
    } else {
      return session.createQuery("from File where parentFolder.id = " + parentFolder.id).list();
    }
  }

  public static void deleteFile(Session session, File file) {
    // we will need to delete all FileComment, FileUserAdvisory and FileUserRating
    List<FileComment> comments = FileCommentHelper.getRootComments(session, file);
    for (FileComment comment : comments) {
      FileCommentHelper.deleteComment(session, comment);
    }
    List<FileUserAdvisory> advisories = FileAdvisoryHelper.getFileUserAdvisories(session, file);
    for (FileUserAdvisory advisory : advisories) {
      session.delete(advisory);
    }
    List<FileUserRating> ratings = FileRatingHelper.getFileUserRatings(session, file);
    for (FileUserRating rating : ratings) {
      session.delete(rating);
    }
    // now delete the FileData if we have some (WITHOUT LOADING IT)
    // must be done via PHP bridge
    
    // also delete all permissions for this
    SecurityHelper.deletePermissions(session, file);
    // ok finally we can delete the file
    session.delete(file);

    Field fields[] = ReflectionCache.getFields(file.getClass());
    for (Field field : fields) {
      try {
        Object obj = field.get(file);
        if (obj instanceof File) {
          deleteFile(session, (File)obj);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
