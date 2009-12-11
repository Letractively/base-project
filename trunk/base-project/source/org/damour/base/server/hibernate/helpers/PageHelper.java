package org.damour.base.server.hibernate.helpers;

import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PageInfo;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.User;
import org.damour.base.client.utils.StringUtils;
import org.hibernate.Session;

public class PageHelper {

  private static String buildQuery(PermissibleObject parent, Class<?> clazz, User authUser, String sortField, boolean sortDescending) {
    String query;

    String orderBy = " order by id " + (sortDescending ? "desc" : "asc");
    if (!StringUtils.isEmpty(sortField)) {
      orderBy = " order by " + sortField + (sortDescending ? " desc" : " asc");
    }

    if (authUser != null && authUser.isAdministrator()) {
      // admin sees all
      query = "from " + clazz.getSimpleName();
      if (parent != null) {
        query += " where parent.id = " + parent.getId();
      }
      query += orderBy;
    } else if (authUser == null) {
      query = "from " + clazz.getSimpleName() + " as obj where obj.globalRead = true";
      if (parent != null) {
        query += " and parent.id = " + parent.getId();
      }
      query += orderBy;
    } else {
      String selectFileUserPerm = "(select perm.permissibleObject.id from " + Permission.class.getSimpleName()
          + " as perm where perm.permissibleObject.id = obj.id and perm.securityPrincipal.id = " + authUser.id + " and perm.readPerm = true)";

      String selectFileGroupPerm = "(select perm.permissibleObject.id from "
          + Permission.class.getSimpleName()
          + " as perm where perm.permissibleObject.id = obj.id and perm.readPerm = true and perm.securityPrincipal.id in (select membership.userGroup.id from GroupMembership as membership where membership.user.id = "
          + authUser.id + "))";

      query = "from " + clazz.getSimpleName() + " as obj where (obj.owner.id = " + authUser.id + " OR obj.globalRead = true OR obj.id in " + selectFileUserPerm
          + " OR obj.id in " + selectFileGroupPerm + ")";
      
      if (parent != null) {
        query += " and parent.id = " + parent.getId();
      }
      query += orderBy;
    }
    return query;
  }

  public static Page<PermissibleObject> getPage(Session session, PermissibleObject parent, Class<?> clazz, User authUser, String sortField, boolean sortDescending, int pageNumber,
      int pageSize) {
    String query = buildQuery(parent, clazz, authUser, sortField, sortDescending);
    GenericPage<PermissibleObject> gPage = new GenericPage<PermissibleObject>(session, clazz, query, pageNumber, pageSize);
    return new Page<PermissibleObject>(gPage.getList(), pageNumber);
  }

  private static long getLastPageNumber(Session session, PermissibleObject parent, Class<?> clazz, User authUser, int pageSize) {
    String query = buildQuery(parent, clazz, authUser, "id", true);
    Long rowCount = (Long) session.createQuery("select count(*) " + query).setCacheable(true).uniqueResult();
    /*
     * We use the Math.floor() method because pageNumber numbers are zero-based (i.e. the first pageNumber is pageNumber 0).
     */
    return new Double(Math.floor((double) (rowCount - 1) / pageSize)).longValue();
  }

  private static long getRowCount(Session session, PermissibleObject parent, Class<?> clazz, User authUser) {
    String query = buildQuery(parent, clazz, authUser, "id", true);
    return (Long) session.createQuery("select count(*) " + query).setCacheable(true).uniqueResult();
  }

  public static PageInfo getPageInfo(Session session, PermissibleObject parent, Class<?> clazz, User authUser, int pageSize) {
    PageInfo pageInfo = new PageInfo();
    long rowCount = getRowCount(session, parent, clazz, authUser);
    long lastPageNumber = new Double(Math.floor((double) (rowCount - 1) / pageSize)).longValue();
    pageInfo.setTotalRowCount(rowCount);
    pageInfo.setLastPageNumber(lastPageNumber);
    return pageInfo;
  }
  
}
