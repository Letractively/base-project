package org.damour.base.server.hibernate.helpers;

import java.util.List;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserThumb;
import org.hibernate.Session;

public class ThumbHelper {

  public static UserThumb getUserThumb(Session session, PermissibleObject permissibleObject, User voter, String voterGUID) {
    if (permissibleObject == null) {
      return null;
    }
    if (voter != null) {
      List<UserThumb> thumbs = session
          .createQuery("from " + UserThumb.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id + " and voter.id = " + voter.id)
          .setCacheable(true).list();
      if (thumbs != null && thumbs.size() > 0) {
        return thumbs.get(0);
      }
      return null;
    }
    List<UserThumb> thumbs = session
        .createQuery(
            "from " + UserThumb.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id + " and voterGUID = '" + voterGUID + "'")
        .setCacheable(true).list();
    if (thumbs != null && thumbs.size() > 0) {
      return thumbs.get(0);
    }

    return null;
  }

  public static List<UserThumb> getUserThumbs(Session session, PermissibleObject permissibleObject) {
    List<UserThumb> thumbs = session.createQuery("from " + UserThumb.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id)
        .setCacheable(true).list();
    return thumbs;
  }

}