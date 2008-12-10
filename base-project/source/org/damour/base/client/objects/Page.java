package org.damour.base.client.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Page<T> implements Serializable {

  public List<T> results = new ArrayList<T>();
  public long totalRowCount;
  public int pageNumber;
  public long lastPageNumber;

  public Page() {
  }

  public Page(List<T> results, int pageNumber, long lastPageNumber, long totalRowCount) {
    this.results.addAll(results);
    this.pageNumber = pageNumber;
    this.totalRowCount = totalRowCount;
    this.lastPageNumber = lastPageNumber;
  }

  public List<T> getResults() {
    return results;
  }

  public void setResults(List<T> results) {
    this.results = results;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

  public long getLastPageNumber() {
    return lastPageNumber;
  }

  public void setLastPageNumber(int lastPageNumber) {
    this.lastPageNumber = lastPageNumber;
  }

  public long getTotalRowCount() {
    return totalRowCount;
  }

  public void setTotalRowCount(long totalRowCount) {
    this.totalRowCount = totalRowCount;
  }

}
