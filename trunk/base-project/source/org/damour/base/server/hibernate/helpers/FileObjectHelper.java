package org.damour.base.server.hibernate.helpers;

import java.lang.reflect.Field;
import java.util.List;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.UserRating;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.UserAdvisory;
import org.damour.base.server.hibernate.ReflectionCache;
import org.hibernate.Session;

public class FileObjectHelper {

  public static List<File> getFiles(Session session, PermissibleObject parentFolder) {
    if (parentFolder == null) {
      return session.createQuery("from File where parentFolder is null").list();
    } else {
      return session.createQuery("from File where parentFolder.id = " + parentFolder.id).list();
    }
  }

  public static void deleteFile(Session session, File file) {
    // we will need to delete all FileComment, FileUserAdvisory and FileUserRating
    List<Comment> comments = CommentHelper.getRootComments(session, file);
    for (Comment comment : comments) {
      CommentHelper.deleteComment(session, comment);
    }
    List<UserAdvisory> advisories = AdvisoryHelper.getUserAdvisories(session, file);
    for (UserAdvisory advisory : advisories) {
      session.delete(advisory);
    }
    List<UserRating> ratings = RatingHelper.getUserRatings(session, file);
    for (UserRating rating : ratings) {
      session.delete(rating);
    }

    session.createQuery("delete FileData where permissibleObject.id = " + file.id).executeUpdate();
    
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
