package org.damour.base.server.hibernate.helpers;

import java.util.List;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserAdvisory;
import org.hibernate.Session;

public class AdvisoryHelper {

  public static UserAdvisory getUserAdvisory(Session session, PermissibleObject permissibleObject, User voter, String voterIP) {
    if (voter != null) {
      List<UserAdvisory> ratings = session.createQuery("from " + UserAdvisory.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id + " and voter.id = " + voter.id).setCacheable(true).list();
      if (ratings != null && ratings.size() > 0) {
        return ratings.get(0);
      }
      return null;
    }
    List<UserAdvisory> ratings = session.createQuery("from " + UserAdvisory.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id + " and voterIP = '" + voterIP + "'").setCacheable(true).list();
    if (ratings != null && ratings.size() > 0) {
      return ratings.get(0);
    }

    return null;
  }

  public static List<UserAdvisory> getUserAdvisories(Session session, PermissibleObject permissibleObject) {
    List<UserAdvisory> ratings = session.createQuery("from " + UserAdvisory.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id).setCacheable(true).list();
    return ratings;
  }

}
