package org.damour.base.server.hibernate.helpers;

import java.util.List;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.FileUserRating;
import org.damour.base.client.objects.User;
import org.hibernate.Session;

public class FileRatingHelper {

  public static FileUserRating getFileUserRating(Session session, File file, User voter, String voterIP) {
    if (voter != null) {
      List<FileUserRating> ratings = session.createQuery("from " + FileUserRating.class.getSimpleName() + " where file.id = " + file.id + " and voter.id = " + voter.id).setCacheable(true).list();
      if (ratings != null && ratings.size() > 0) {
        return ratings.get(0);
      }
      return null;
    }
    List<FileUserRating> ratings = session.createQuery("from " + FileUserRating.class.getSimpleName() + " where file.id = " + file.id + " and voterIP = '" + voterIP + "'").setCacheable(true).list();
    if (ratings != null && ratings.size() > 0) {
      return ratings.get(0);
    }

    return null;
  }

  public static List<FileUserRating> getFileUserRatings(Session session, File file) {
    List<FileUserRating> ratings = session.createQuery("from " + FileUserRating.class.getSimpleName() + " where file.id = " + file.id).setCacheable(true).list();
    return ratings;
  }
}
