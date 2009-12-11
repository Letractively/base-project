package org.damour.base.client.objects;

import java.io.Serializable;

public class PageInfo implements Serializable {

  public long totalRowCount;
  public long lastPageNumber;

  public PageInfo() {
  }

  public long getTotalRowCount() {
    return totalRowCount;
  }

  public void setTotalRowCount(long totalRowCount) {
    this.totalRowCount = totalRowCount;
  }

  public long getLastPageNumber() {
    return lastPageNumber;
  }

  public void setLastPageNumber(long lastPageNumber) {
    this.lastPageNumber = lastPageNumber;
  }

}
