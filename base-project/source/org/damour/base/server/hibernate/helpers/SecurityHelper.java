package org.damour.base.server.hibernate.helpers;

import java.util.List;

import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.PendingGroupMembership;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.SecurityPrincipal;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.objects.Permission.PERM;
import org.hibernate.Session;

public class SecurityHelper {

  public static void deletePermissions(Session session, PermissibleObject object) {
    session.createQuery("delete from " + Permission.class.getSimpleName() + " where permissibleObject.id = " + object.id).setCacheable(true).executeUpdate();
  }

  public static List<Permission> getPermissions(Session session, SecurityPrincipal principal, PermissibleObject object) {
    return session.createQuery(
        "from " + Permission.class.getSimpleName() + " where securityPrincipal.id = " + principal.id + " and permissibleObject.id = " + object.id)
        .setCacheable(true).list();
  }

  public static List<Permission> getPermissions(Session session, PermissibleObject object) {
    return session.createQuery("from " + Permission.class.getSimpleName() + " where permissibleObject.id = " + object.id).setCacheable(true).list();
  }

  public static List<GroupMembership> getUserGroupMemberhips(Session session, User user) {
    return session.createQuery("from " + GroupMembership.class.getSimpleName() + " where user.id = " + user.id).setCacheable(true).list();
  }

  public static List<GroupMembership> getUserGroupMemberhips(Session session) {
    return session.createQuery("from " + GroupMembership.class.getSimpleName()).setCacheable(true).list();
  }

  public static List<User> getUsersInUserGroup(Session session, UserGroup group) {
    // select user from GroupMembership where userGroup.id = group.id
    return session.createQuery("select user from " + GroupMembership.class.getSimpleName() + " groupMem where groupMem.userGroup.id = " + group.id)
        .setCacheable(true).list();
  }

  public static List<UserGroup> getUserGroups(Session session, User user) {
    // select distinct userGroup from GroupMembership where user.id = user.id
    return session.createQuery("select distinct userGroup from " + GroupMembership.class.getSimpleName() + " groupMem where groupMem.user.id = " + user.id)
        .setCacheable(true).list();
  }

  public static List<UserGroup> getVisibleUserGroups(Session session, User user) {
    // select distinct userGroup from GroupMembership where user.id = user.id
    return session
        .createQuery(
            "select distinct userGroup from " + GroupMembership.class.getSimpleName() + " groupMem where groupMem.visible = true and groupMem.user.id = "
                + user.id).setCacheable(true).list();
  }

  public static List<UserGroup> getOwnedUserGroups(Session session, User user) {
    return session.createQuery("from " + UserGroup.class.getSimpleName() + " where owner.id = " + user.id).setCacheable(true).list();
  }

  public static List<UserGroup> getOwnedVisibleUserGroups(Session session, User user) {
    return session.createQuery("from " + UserGroup.class.getSimpleName() + " where visible = true and owner.id = " + user.id).setCacheable(true).list();
  }

  public static List<PendingGroupMembership> getPendingGroupMemberships(Session session, User user) {
    if (user.isAdministrator()) {
      return session.createQuery("from " + PendingGroupMembership.class.getSimpleName()).setCacheable(true).list();
    } else {
      return session.createQuery("from " + PendingGroupMembership.class.getSimpleName() + " where userGroup.owner.id = " + user.id).setCacheable(true).list();
    }
  }

  public static List<PendingGroupMembership> getPendingGroupMemberships(Session session, UserGroup group) {
    return session.createQuery("from " + PendingGroupMembership.class.getSimpleName() + " where userGroup.id = " + group.id).setCacheable(true).list();
  }

  public static List<UserGroup> getUserGroups(Session session) {
    return session.createQuery("from " + UserGroup.class.getSimpleName()).setCacheable(true).list();
  }

  public static List<UserGroup> getVisibleUserGroups(Session session) {
    return session.createQuery("from " + UserGroup.class.getSimpleName() + " where visible = true").setCacheable(true).list();
  }

  public static List<String> getUsernames(Session session) {
    return session.createQuery("select username from " + User.class.getSimpleName()).setCacheable(true).list();
  }

  public static List<User> getUsers(Session session) {
    return session.createQuery("from " + User.class.getSimpleName()).setCacheable(true).list();
  }

  public static List<GroupMembership> getGroupMemberships(Session session, User user) {
    return session.createQuery("from " + GroupMembership.class.getSimpleName() + " where user.id = " + user.id).setCacheable(true).list();
  }

  public static List<GroupMembership> getGroupMemberships(Session session, UserGroup group) {
    return session.createQuery("from " + GroupMembership.class.getSimpleName() + " where userGroup.id = " + group.id).setCacheable(true).list();
  }

  public static Folder getFolder(Session session, Folder parentFolder, String folderName) {
    if (parentFolder == null) {
      List<Folder> folders = session.createQuery("from " + Folder.class.getSimpleName() + " where parentFolder is null and name = '" + folderName + "'")
          .setCacheable(true).list();
      if (folders != null && folders.size() > 0) {
        return folders.get(0);
      }
      return null;
    }
    List<Folder> folders = session.createQuery("from " + Folder.class.getSimpleName() + " where parentFolder.id = " + parentFolder.id).setCacheable(true)
        .list();
    if (folders != null && folders.size() > 0) {
      return folders.get(0);
    }
    return null;
  }

  public static GroupMembership getGroupMembership(Session session, User user, UserGroup group) {
    List<GroupMembership> memberships = session.createQuery(
        "from " + GroupMembership.class.getSimpleName() + " where userGroup.id = " + group.id + " and user.id = " + user.id).setCacheable(true).list();
    if (memberships != null && memberships.size() > 0) {
      return memberships.get(0);
    }
    return null;
  }

  public static UserGroup getUserGroup(Session session, String groupName) {
    List<UserGroup> groups = session.createQuery("from " + UserGroup.class.getSimpleName() + " where name = '" + groupName + "'").setCacheable(true).list();
    if (groups != null && groups.size() > 0) {
      return groups.get(0);
    }
    return null;
  }

  public static void deleteUserGroup(Session session, UserGroup group) {
    // delete all group memberships
    List<GroupMembership> memberships = getGroupMemberships(session, group);
    for (GroupMembership membership : memberships) {
      session.delete(membership);
    }
    session.delete(group);
  }

  private static boolean hasGlobalPerm(PERM permission, PermissibleObject object) {
    if (permission.equals(PERM.READ) && object.isGlobalRead()) {
      return true;
    } else if (permission.equals(PERM.WRITE) && object.isGlobalWrite()) {
      return true;
    } else if (permission.equals(PERM.EXECUTE) && object.isGlobalExecute()) {
      return true;
    }
    return false;
  }

  private static boolean hasPerm(PERM permission, Permission perm) {
    if (permission.equals(PERM.READ) && perm.isReadPerm()) {
      return true;
    } else if (permission.equals(PERM.WRITE) && perm.isWritePerm()) {
      return true;
    } else if (permission.equals(PERM.EXECUTE) && perm.isExecutePerm()) {
      return true;
    }
    return false;
  }

  public static boolean doesUserHavePermission(Session session, User user, PermissibleObject object, PERM permission) {

    // if the object is null, we're out early (but allow it)
    if (object == null) {
      return true;
    }

    // if the content has global "wide open" perms
    if (hasGlobalPerm(permission, object)) {
      return true;
    }

    // at this point we must have a valid user
    if (user == null) {
      return false;
    }

    // the admin and owner always have perms
    if (user.isAdministrator() || user.equals(object.getOwner())) {
      return true;
    }

    // ok now the hard part
    // user permissions
    List<Permission> perms = getPermissions(session, user, object);
    for (Permission perm : perms) {
      if (hasPerm(permission, perm)) {
        return true;
      }
    }
    // ok no user perms, do group permissions
    List<GroupMembership> groupMemberships = getUserGroupMemberhips(session, user);
    for (GroupMembership groupMembership : groupMemberships) {
      List<Permission> groupPerms = getPermissions(session, groupMembership.getUserGroup(), object);
      for (Permission perm : groupPerms) {
        if (hasPerm(permission, perm)) {
          return true;
        }
      }
    }
    return false;
  }

}
