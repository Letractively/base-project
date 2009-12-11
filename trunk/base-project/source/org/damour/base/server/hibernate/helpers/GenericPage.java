package org.damour.base.server.hibernate.helpers;

import java.util.List;

import org.damour.base.client.utils.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class GenericPage<T> {

  private List<T> results;
  private int pageSize;
  private int pageNumber;
  private long rowCount = 0;

  private Session session;
  private Class<?> clazz;
  private String query;

  /**
   * Construct a new GenericPage. GenericPage numbers are zero-based, so the first pageNumber is pageNumber 0.
   * 
   * @param query
   *          the Hibernate Query
   * @param pageNumber
   *          the pageNumber number (zero-based)
   * @param pageSize
   *          the number of results to display on the pageNumber
   */
  public GenericPage(Session session, Class<?> clazz, String query, int page, int pageSize) {
    this.pageNumber = page;
    this.pageSize = pageSize;
    this.session = session;
    if (StringUtils.isEmpty(query)) {
      this.query = "from " + clazz.getSimpleName();
    } else {
      this.query = query;
    }
    try {
      /*
       * We set the max results to one more than the specfied pageSize to determine if any more results exist (i.e. if there is a next pageNumber to display).
       * The result set is trimmed down to just the pageSize before being displayed later (in getList()).
       */
      results = (List<T>) session.createQuery(query).setFirstResult(page * pageSize).setCacheable(true).setMaxResults(pageSize + 1).list();
    } catch (HibernateException e) {
      e.printStackTrace();
    }
  }

  public GenericPage<T> next() {
    GenericPage<T> pager = new GenericPage<T>(session, clazz, query, getNextPageNumber(), pageSize);
    return pager;
  }

  public long getLastPageNumber() {
    if (StringUtils.isEmpty(query)) {
      rowCount = (Long) session.createQuery("select count(*) from " + clazz.getSimpleName()).setCacheable(true).uniqueResult();
    } else {
      rowCount = (Long) session.createQuery("select count(*) " + query).setCacheable(true).uniqueResult();
    }
    /*
     * We use the Math.floor() method because pageNumber numbers are zero-based (i.e. the first pageNumber is pageNumber 0).
     */
    return new Double(Math.floor((double) (rowCount - 1) / pageSize)).longValue();
  }

  public long getPageCount() {
    return getLastPageNumber() + 1;
  }

  public List<T> getList() {
    return results.size() > pageSize ? results.subList(0, pageSize) : results;
  }

  public int getNextPageNumber() {
    return pageNumber + 1;
  }

  public int getPreviousPageNumber() {
    return pageNumber - 1;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public long getRowCount() {
    return rowCount;
  }

}
