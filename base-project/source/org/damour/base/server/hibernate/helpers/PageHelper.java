package org.damour.base.server.hibernate.helpers;

import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.User;
import org.hibernate.Session;

public class PageHelper {

  public static Page<PermissibleObject> getPage(Session session, Class<?> clazz, User authUser, boolean sortDescending, int pageNumber, int pageSize) {
    String query;

    if (authUser != null && authUser.isAdministrator()) {
      // admin sees all
      query = "from " + clazz.getSimpleName() + " order by id " + (sortDescending ? "desc" : "asc");
    } else if (authUser == null) {
      query = "from " + clazz.getSimpleName() + " as obj where obj.globalRead = true order by id " + (sortDescending ? "desc" : "asc");
    } else {
      String selectFileUserPerm = "(select perm.permissibleObject.id from " + Permission.class.getSimpleName()
          + " as perm where perm.permissibleObject.id = obj.id and perm.securityPrincipal.id = " + authUser.id + " and perm.readPerm = true)";

      String selectFileGroupPerm = "(select perm.permissibleObject.id from "
          + Permission.class.getSimpleName()
          + " as perm where perm.permissibleObject.id = obj.id and perm.readPerm = true and perm.securityPrincipal.id in (select membership.userGroup.id from GroupMembership as membership where membership.user.id = "
          + authUser.id + "))";

      query = "from " + clazz.getSimpleName() + " as obj where (obj.owner.id = " + authUser.id + " OR obj.globalRead = true OR obj.id in " + selectFileUserPerm
          + " OR obj.id in " + selectFileGroupPerm + ") order by id " + (sortDescending ? "desc" : "asc");
    }

    GenericPage<PermissibleObject> gPage = new GenericPage<PermissibleObject>(session, query, pageNumber, pageSize);
    return new Page<PermissibleObject>(gPage.getList(), pageNumber, gPage.getLastPageNumber(), gPage.getRowCount());
  }

}
