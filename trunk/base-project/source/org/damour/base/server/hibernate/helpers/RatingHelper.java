package org.damour.base.server.hibernate.helpers;

import java.util.List;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserRating;
import org.hibernate.Session;

public class RatingHelper {

  public static UserRating getUserRating(Session session, PermissibleObject permissibleObject, User voter, String voterGUID) {
    if (permissibleObject == null) {
      return null;
    }
    if (voter != null) {
      List<UserRating> ratings = session
          .createQuery("from " + UserRating.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id + " and voter.id = " + voter.id)
          .setCacheable(true).list();
      if (ratings != null && ratings.size() > 0) {
        return ratings.get(0);
      }
      return null;
    }
    List<UserRating> ratings = session
        .createQuery(
            "from " + UserRating.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id + " and voterGUID = '" + voterGUID + "'")
        .setCacheable(true).list();
    if (ratings != null && ratings.size() > 0) {
      return ratings.get(0);
    }

    return null;
  }

  public static List<UserRating> getUserRatings(Session session, PermissibleObject permissibleObject) {
    List<UserRating> ratings = session.createQuery("from " + UserRating.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id)
        .setCacheable(true).list();
    return ratings;
  }

  public static PermissibleObject getNextUnratedPermissibleObject(Session session, String objectType, User voter, String voterGUID) {
    List<PermissibleObject> objects = null;
    Class clazz = PermissibleObject.class;
    try {
      clazz = Class.forName(objectType);
    } catch (Throwable t) {
    }
    if (voter != null) {
      String query = "from " + clazz.getSimpleName() + " where id not in (select rating.permissibleObject.id from " + UserRating.class.getSimpleName()
          + " rating where rating.voter.id = " + voter.getId() + ")";
      System.out.println(query);
      objects = session.createQuery(query).setCacheable(true).setMaxResults(1).list();
    } else {
      // go based on voterIP
      objects = session
          .createQuery(
              "from " + clazz.getSimpleName() + " where id not in (select permissibleObject.id from " + UserRating.class.getSimpleName()
                  + " where voterGUID = '" + voterGUID + "')").setCacheable(true).setMaxResults(1).list();
    }

    if (objects != null && objects.size() > 0) {
      return objects.get(0);
    }
    return null;
  }
}
