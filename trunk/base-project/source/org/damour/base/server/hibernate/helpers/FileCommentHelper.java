package org.damour.base.server.hibernate.helpers;

import java.util.List;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.FileComment;
import org.hibernate.Session;

public class FileCommentHelper {

  public static List<FileComment> getRootComments(Session session, File file) {
    return (List<FileComment>) session.createQuery("from " + FileComment.class.getSimpleName() + " where parentComment is null and file.id = " + file.id).setCacheable(true).list();
  }

  
  public static List<FileComment> getComments(Session session, File file) {
    return (List<FileComment>) session.createQuery("from " + FileComment.class.getSimpleName() + " where file.id = " + file.id).setCacheable(true).list();
  }

  public static List<FileComment> getComments(Session session, FileComment fileComment) {
    return (List<FileComment>) session.createQuery("from " + FileComment.class.getSimpleName() + " where parentComment.id = " + fileComment.id).setCacheable(true).list();
  }
  
  public static void deleteComment(Session session, FileComment parentComment) {
    if (parentComment == null) {
      return;
    }
    List<FileComment> children = getComments(session, parentComment);
    for (FileComment childComment : children) {
      deleteComment(session, childComment);
    }
    session.delete(parentComment);
  }
  
}
