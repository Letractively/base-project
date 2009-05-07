package org.damour.base.server.hibernate.helpers;

import java.lang.reflect.Field;
import java.util.List;

import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.UserAdvisory;
import org.damour.base.client.objects.UserRating;
import org.damour.base.server.hibernate.ReflectionCache;
import org.hibernate.Session;

public class PermissibleObjectHelper {
  public static void deletePermissibleObject(Session session, PermissibleObject permissibleObject) {
    if (permissibleObject == null) {
      return;
    }

    // we will need to delete all FileComment, FileUserAdvisory and FileUserRating
    List<Comment> comments = CommentHelper.getRootComments(session, permissibleObject);
    for (Comment comment : comments) {
      CommentHelper.deleteComment(session, comment);
    }
    List<UserAdvisory> advisories = AdvisoryHelper.getUserAdvisories(session, permissibleObject);
    for (UserAdvisory advisory : advisories) {
      session.delete(advisory);
    }
    List<UserRating> ratings = RatingHelper.getUserRatings(session, permissibleObject);
    for (UserRating rating : ratings) {
      session.delete(rating);
    }

    session.createQuery("delete FileData where permissibleObject.id = " + permissibleObject.id).executeUpdate();

    // also delete all permissions for this
    SecurityHelper.deletePermissions(session, permissibleObject);
    // ok finally we can delete the file
    session.delete(permissibleObject);

    Field fields[] = ReflectionCache.getFields(permissibleObject.getClass());
    for (Field field : fields) {
      if (!field.getName().equals("parent") && PermissibleObject.class.isAssignableFrom(field.getType())) {
        try {
          Object obj = field.get(permissibleObject);
          deletePermissibleObject(session, (PermissibleObject) obj);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static List<PermissibleObject> getChildren(Session session, PermissibleObject parent) {
    if (parent == null) {
      return session.createQuery("from PermissibleObject where parent is null").list();
    } else {
      return session.createQuery("from PermissibleObject where parent.id = " + parent.id).list();
    }
  }
}