package org.damour.base.server.hibernate.helpers;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserAdvisory;
import org.damour.base.client.objects.UserRating;
import org.damour.base.server.hibernate.HibernateUtil;
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

    List<Field> fields = ReflectionCache.getFields(permissibleObject.getClass());
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

  public static List<PermissibleObject> getMyPermissibleObjects(Session session, User owner, PermissibleObject parent) {
    if (parent == null) {
      return session.createQuery("from PermissibleObject where owner.id = " + owner.id + " order by creationDate desc").setCacheable(true).list();
    } else {
      return session.createQuery("from PermissibleObject where parent.id = " + parent.id + " and owner.id = " + owner.id + " order by creationDate desc")
          .setCacheable(true).list();
    }
  }

  public static List<PermissibleObject> getMyPermissibleObjects(Session session, User owner, PermissibleObject parent, Class instanceType) {
    if (parent == null) {
      return session.createQuery("from " + instanceType.getSimpleName() + " where owner.id = " + owner.id + " order by creationDate desc").setCacheable(true)
          .list();
    } else {
      return session.createQuery(
          "from " + instanceType.getSimpleName() + " where parent.id = " + parent.id + " and owner.id = " + owner.id + " order by creationDate desc")
          .setCacheable(true).list();
    }
  }

  public static List<PermissibleObject> search(Session session, Class searchObjectType, String userQuery, boolean searchNames, boolean searchDescriptions,
      boolean searchKeywords, boolean useExactPhrase) {
    if (userQuery == null) {
      return Collections.emptyList();
    }
    userQuery = userQuery.replaceAll("'", "");

    String query = "from " + searchObjectType.getSimpleName();

    if (useExactPhrase) {
      boolean addedWhere = false;
      if (searchNames) {
        if (!addedWhere) {
          query += " where ";
        }
        query += "(lower(name) like '%" + userQuery.toLowerCase() + "%')";
        addedWhere = true;
      }
      if (searchDescriptions) {
        if (!addedWhere) {
          addedWhere = true;
          query += " where ";
        } else {
          query += " or ";
        }
        query += "(lower(description) like '%" + userQuery.toLowerCase() + "%')";
      }
      if (searchKeywords) {
        if (!addedWhere) {
          addedWhere = true;
          query += " where ";
        } else {
          query += " or ";
        }
        query += "(lower(keywords) like '%" + userQuery.toLowerCase() + "%')";
      }
    } else {
      StringTokenizer st = new StringTokenizer(userQuery, " ,()");
      boolean addedWhere = false;
      while (st.hasMoreTokens()) {
        String token = st.nextToken().toLowerCase();
        if (searchNames) {
          if (!addedWhere) {
            query += " where ";
            addedWhere = true;
          } else {
            query += " or ";
          }
          query += "lower(name) like '%" + token + "%'";
        }
        if (searchDescriptions) {
          if (!addedWhere) {
            query += " where ";
            addedWhere = true;
          } else {
            query += " or ";
          }
          query += "lower(description) like '%" + token + "%'";
        }
        if (searchKeywords) {
          if (!addedWhere) {
            query += " where ";
            addedWhere = true;
          } else {
            query += " or ";
          }
          query += "lower(keywords) like '%" + token + "%'";
        }
      }
    }

    query += " order by name asc";
    return HibernateUtil.getInstance().executeQuery(session, query, true);
  }

}
