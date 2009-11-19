package org.damour.base.server.hibernate.helpers;

import java.util.ArrayList;
import java.util.List;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Tag;
import org.damour.base.client.objects.TagMembership;
import org.hibernate.Session;

public class TagHelper {

  public static List<Tag> getTags(Session session) {
    return session.createQuery("from " + Tag.class.getSimpleName()).list();
  }

  public static List<Tag> getTags(Session session, Tag parentTag) {
    if (parentTag == null) {
      return session.createQuery("from " + Tag.class.getSimpleName() + " where parentTag is null").list();
    } else {
      return session.createQuery("from " + Tag.class.getSimpleName() + " where parentTag.id = " + parentTag.id).list();
    }
  }

  public static List<Tag> getTags(Session session, PermissibleObject permissibleObject) {
    return session.createQuery(
        "from " + Tag.class.getSimpleName()
            + " where id in (select tagMembership.permissibleObject.id from TagMembership as tagMembership where permissibleObject.id = "
            + permissibleObject.id + ")").list();
  }

  public static List<TagMembership> getTagMemberships(Session session, Tag tag) {
    if (tag == null) {
      return session.createQuery("from " + TagMembership.class.getSimpleName() + " where tag is null").list();
    } else {
      return session.createQuery("from " + TagMembership.class.getSimpleName() + " where tag.id = " + tag.id).list();
    }
  }

  public static TagMembership getTagMembership(Session session, Tag tag, PermissibleObject permissibleObject) {
    List<TagMembership> tagMems = new ArrayList<TagMembership>();
    if (tag == null) {
      tagMems = session.createQuery("from " + TagMembership.class.getSimpleName() + " where tag is null and permissibleObject.id = " + permissibleObject.id)
          .list();
    } else {
      tagMems = session.createQuery(
          "from " + TagMembership.class.getSimpleName() + " where tag.id = " + tag.id + " and permissibleObject.id = " + permissibleObject.id).list();
    }
    if (tagMems != null && tagMems.size() > 0) {
      return tagMems.get(0);
    }
    return null;
  }

  public static void deleteTag(Session session, Tag tag) {
    // delete all TagMemberships
    List<TagMembership> tags = getTagMemberships(session, tag);
    for (TagMembership membership : tags) {
      session.delete(membership);
    }

    // delete all sub-tags in this folder
    List<Tag> subTags = TagHelper.getTags(session, tag);
    for (Tag subTag : subTags) {
      deleteTag(session, subTag);
    }
    session.delete(tag);
  }

  public static void main(String args[]) {

  }

}
