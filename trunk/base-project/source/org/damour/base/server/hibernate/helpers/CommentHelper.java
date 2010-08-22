package org.damour.base.server.hibernate.helpers;

import java.util.List;

import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.PermissibleObject;
import org.hibernate.Session;

public class CommentHelper {

  public static List<Comment> getRootComments(Session session, PermissibleObject permissibleObject) {
    return (List<Comment>) session.createQuery("from " + Comment.class.getSimpleName() + " where parentComment is null and id = " + permissibleObject.id).setCacheable(true).list();
  }

  
  public static List<Comment> getComments(Session session, PermissibleObject permissibleObject) {
    return (List<Comment>) session.createQuery("from " + Comment.class.getSimpleName() + " where id = " + permissibleObject.id).setCacheable(true).list();
  }

  public static List<Comment> getComments(Session session, Comment fileComment) {
    return (List<Comment>) session.createQuery("from " + Comment.class.getSimpleName() + " where parentComment.id = " + fileComment.id).setCacheable(true).list();
  }
  
  public static void deleteComment(Session session, Comment parentComment) {
    if (parentComment == null) {
      return;
    }
    List<Comment> children = getComments(session, parentComment);
    for (Comment childComment : children) {
      deleteComment(session, childComment);
    }
    session.delete(parentComment);
  }
  
}
