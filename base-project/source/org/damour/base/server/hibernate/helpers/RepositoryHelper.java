package org.damour.base.server.hibernate.helpers;

import java.util.List;
import java.util.Set;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.RepositoryTreeNode;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.Permission.PERM;
import org.hibernate.Session;

public class RepositoryHelper {

  public static void buildRepositoryTreeNode(Session session, User user, RepositoryTreeNode parentNode, Folder parentFolder) {
    if (!SecurityHelper.doesUserHavePermission(session, user, parentFolder, PERM.READ)) {
      return;
    }
    String selectFolderUserPerm = "(select perm.permissibleObject.id from " + Permission.class.getSimpleName() + " as perm where perm.permissibleObject.id = folder.id and perm.securityPrincipal.id = " + user.id
        + " and perm.readPerm = true)";
    String selectFolderGroupPerm = "(select perm.permissibleObject.id from " + Permission.class.getSimpleName()
        + " as perm where perm.permissibleObject.id = folder.id and perm.readPerm = true and perm.securityPrincipal.id in (select membership.userGroup.id from GroupMembership as membership where membership.user.id = " + user.id + "))";

    String selectFileUserPerm = "(select perm.permissibleObject.id from " + Permission.class.getSimpleName() + " as perm where perm.permissibleObject.id = file.id and perm.securityPrincipal.id = " + user.id + " and perm.readPerm = true)";
    String selectFileGroupPerm = "(select perm.permissibleObject.id from " + Permission.class.getSimpleName()
        + " as perm where perm.permissibleObject.id = file.id and perm.readPerm = true and perm.securityPrincipal.id in (select membership.userGroup.id from GroupMembership as membership where membership.user.id = " + user.id + "))";

    List<Folder> folders = null;
    if (parentFolder == null) {
      if (user != null && user.isAdministrator()) {
        // admin sees all
        folders = session.createQuery("from " + Folder.class.getSimpleName() + " as folder where folder.parentFolder is null").setCacheable(true).list();
      } else if (user == null) {
        folders = session.createQuery(
            "from " + Folder.class.getSimpleName() + " as folder where folder.parentFolder is null folder.globalRead = true").setCacheable(true).list();
      } else {
        folders = session.createQuery(
            "from " + Folder.class.getSimpleName() + " as folder where folder.parentFolder is null and (folder.owner.id = " + user.id + " OR folder.globalRead = true OR folder.id in " + selectFolderUserPerm + " OR folder.id in "
                + selectFolderGroupPerm + ")").setCacheable(true).list();
      }
    } else {
      if (user != null && user.isAdministrator()) {
        // admin sees all
        folders = session.createQuery("from " + Folder.class.getSimpleName() + " as folder where folder.parentFolder.id = " + parentFolder.id).setCacheable(true).list();
      } else if (user == null) {
        folders = session.createQuery(
            "from " + Folder.class.getSimpleName() + " as folder where folder.parentFolder.id = " + parentFolder.id + " and folder.globalRead = true").setCacheable(true).list();
      } else {
        folders = session.createQuery(
            "from " + Folder.class.getSimpleName() + " as folder where folder.parentFolder.id = " + parentFolder.id + " and (folder.owner.id = " + user.id + " OR folder.globalRead = true OR folder.id in " + selectFolderUserPerm
                + " OR folder.id in " + selectFolderGroupPerm + ")").setCacheable(true).list();
      }
    }
    List<File> files = null;
    if (parentFolder == null) {
      if (user != null && user.isAdministrator()) {
        // admin sees all
        files = session.createQuery("from " + File.class.getSimpleName() + " as file where file.parentFolder is null").setCacheable(true).list();
      } else if (user == null) {
        files = session.createQuery(
            "from " + File.class.getSimpleName() + " as file where file.parentFolder is null and file.globalRead = true").setCacheable(true).list();
      } else {
        files = session.createQuery(
            "from " + File.class.getSimpleName() + " as file where file.parentFolder is null and (file.owner.id = " + user.id + " OR file.globalRead = true OR file.id in " + selectFileUserPerm + " OR file.id in " + selectFileGroupPerm
                + ")").setCacheable(true).list();
      }
    } else {
      if (user != null && user.isAdministrator()) {
        // admin sees all
        files = session.createQuery("from " + File.class.getSimpleName() + " as file where file.parentFolder.id = " + parentFolder.id).setCacheable(true).list();
      } else if (user == null) {
        files = session.createQuery(
            "from " + File.class.getSimpleName() + " as file where file.parentFolder.id = " + parentFolder.id + " and file.globalRead = true").setCacheable(true).list();
      } else {
        files = session.createQuery(
            "from " + File.class.getSimpleName() + " as file where file.parentFolder.id = " + parentFolder.id + " and (file.owner.id = " + user.id + " OR file.globalRead = true OR file.id in " + selectFileUserPerm + " OR file.id in "
                + selectFileGroupPerm + ")").setCacheable(true).list();
      }
    }
    for (Folder folder : folders) {
      RepositoryTreeNode childNode = new RepositoryTreeNode();
      parentNode.getFolders().put(folder, childNode);
      buildRepositoryTreeNode(session, user, childNode, folder);
    }
    parentNode.setFiles(files);
  }

  public static void dumpTreeNode(RepositoryTreeNode parent, int depth) {
    Set<Folder> folders = parent.getFolders().keySet();
    List<File> files = parent.getFiles();

    if (folders != null) {
      for (Folder folder : folders) {
        for (int i = 0; i < depth; i++) {
          System.out.print("   ");
        }
        System.out.println("FOLDER: " + folder.getName());
      }
    }
    if (files != null) {
      for (File file : files) {
        for (int i = 0; i < depth; i++) {
          System.out.print("   ");
        }
        System.out.println("FILE: " + file.getName());
      }
    }
    for (RepositoryTreeNode childNode : parent.getFolders().values()) {
      dumpTreeNode(childNode, depth + 1);
    }
  }

}
