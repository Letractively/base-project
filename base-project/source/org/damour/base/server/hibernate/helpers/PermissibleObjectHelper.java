package org.damour.base.server.hibernate.helpers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserAdvisory;
import org.damour.base.client.objects.UserRating;
import org.damour.base.client.objects.UserThumb;
import org.damour.base.client.utils.StringUtils;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.ReflectionCache;
import org.hibernate.Session;

public class PermissibleObjectHelper {
  public static void deletePermissibleObject(Session session, PermissibleObject permissibleObject) {
    if (permissibleObject == null) {
      return;
    }

    // we will need to delete all FileComment, FileUserAdvisory and FileUserRating
    if (permissibleObject.getNumComments() > 0) {
      List<Comment> comments = CommentHelper.getRootComments(session, permissibleObject);
      for (Comment comment : comments) {
        CommentHelper.deleteComment(session, comment);
      }
    }
    if (permissibleObject.getNumAdvisoryVotes() > 0) {
      List<UserAdvisory> advisories = AdvisoryHelper.getUserAdvisories(session, permissibleObject);
      for (UserAdvisory advisory : advisories) {
        session.delete(advisory);
      }
    }
    if (permissibleObject.getNumRatingVotes() > 0) {
      List<UserRating> ratings = RatingHelper.getUserRatings(session, permissibleObject);
      for (UserRating rating : ratings) {
        session.delete(rating);
      }
    }
    if (permissibleObject.getNumUpVotes() > 0 || permissibleObject.getNumDownVotes() > 0) {
      List<UserThumb> thumbs = ThumbHelper.getUserThumbs(session, permissibleObject);
      for (UserThumb thumb : thumbs) {
        session.delete(thumb);
      }
    }

    session.createQuery("delete FileData where permissibleObject.id = " + permissibleObject.id).executeUpdate();

    // also delete all permissions for this
    SecurityHelper.deletePermissions(session, permissibleObject);

    // delete children
    List<PermissibleObject> children = getChildren(session, permissibleObject);
    for (PermissibleObject child : children) {
      deletePermissibleObject(session, child);
    }

    // ok finally we can delete the file
    System.out.println("deleting: " + permissibleObject.getClass().getName() + "->" + permissibleObject.getId());
    session.delete(permissibleObject);   
    session.flush();
    
    List<Field> fields = ReflectionCache.getFields(permissibleObject.getClass());
    for (Field field : fields) {
      if (!field.getName().startsWith("parent") && PermissibleObject.class.isAssignableFrom(field.getType())) {
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

  public static List<PermissibleObject> getMyPermissibleObjects(Session session, User owner, PermissibleObject parent, Class<?> instanceType) {
    if (parent == null) {
      return session.createQuery("from " + instanceType.getSimpleName() + " where owner.id = " + owner.id + " order by creationDate desc").setCacheable(true)
          .list();
    } else {
      return session
          .createQuery(
              "from " + instanceType.getSimpleName() + " where parent.id = " + parent.id + " and owner.id = " + owner.id + " order by creationDate desc")
          .setCacheable(true).list();
    }
  }

  public static List<PermissibleObjectTreeNode> search(Session session, User user, String voterGUID, Class<?> searchObjectType, String userQuery,
      String sortField, boolean sortDescending, boolean searchNames, boolean searchDescriptions, boolean searchKeywords, boolean useExactPhrase) {
    if (userQuery == null) {
      return Collections.emptyList();
    }
    userQuery = userQuery.replaceAll("'", "");

    String orderBy = " order by name " + (sortDescending ? "desc" : "asc");
    if (!StringUtils.isEmpty(sortField)) {
      orderBy = " order by " + sortField + (sortDescending ? " desc" : " asc");
    }

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

    query += orderBy;

    List<PermissibleObject> objects = HibernateUtil.getInstance().executeQuery(session, query, true);
    List<PermissibleObjectTreeNode> treeNodes = new ArrayList<PermissibleObjectTreeNode>();
    for (PermissibleObject object : objects) {
      PermissibleObjectTreeNode treeNode = new PermissibleObjectTreeNode();
      treeNode.setObject(object);
      treeNodes.add(treeNode);
      if (object.getNumAdvisoryVotes() > 0) {
        treeNode.setUserAdvisory(AdvisoryHelper.getUserAdvisory(session, object, user, voterGUID));
      }
      if (object.getNumRatingVotes() > 0) {
        treeNode.setUserRating(RatingHelper.getUserRating(session, object, user, voterGUID));
      }
      if (object.getNumUpVotes() > 0 || object.getNumDownVotes() > 0) {
        treeNode.setUserThumb(ThumbHelper.getUserThumb(session, object, user, voterGUID));
      }
    }
    return treeNodes;
  }

}
