package org.damour.base.client.objects;

import java.io.Serializable;

public class MemoryStats implements Serializable {

  public long freeMemory;
  public long totalMemory;
  public long maxMemory;

  public MemoryStats() {
  }

  public long getFreeMemory() {
    return freeMemory;
  }

  public void setFreeMemory(long freeMemory) {
    this.freeMemory = freeMemory;
  }

  public long getTotalMemory() {
    return totalMemory;
  }

  public void setTotalMemory(long totalMemory) {
    this.totalMemory = totalMemory;
  }

  public long getMaxMemory() {
    return maxMemory;
  }

  public void setMaxMemory(long maxMemory) {
    this.maxMemory = maxMemory;
  }

}
