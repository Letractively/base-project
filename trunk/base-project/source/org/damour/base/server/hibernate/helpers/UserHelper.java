package org.damour.base.server.hibernate.helpers;

import java.util.List;

import org.damour.base.client.objects.User;
import org.hibernate.Session;

public class UserHelper {

  public static User getUser(Session session, String username) {
    List<User> users = session.createQuery("from User where username = '" + username + "'").setCacheable(true).list();
    if (users != null && users.size() > 0) {
      return users.get(0);
    }
    return null;
  }

}
