package org.damour.base.client.objects;

import java.io.Serializable;

public class HibernateStat implements Serializable {

  public String regionName;
  public long cachePuts;
  public long cacheHits;
  public long cacheMisses;
  public long numObjectsInCache;
  public long memoryUsed;
  public long numObjectsOnDisk;

  public HibernateStat() {
  }

  public String getRegionName() {
    return regionName;
  }

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public long getCachePuts() {
    return cachePuts;
  }

  public void setCachePuts(long cachePuts) {
    this.cachePuts = cachePuts;
  }

  public long getCacheHits() {
    return cacheHits;
  }

  public void setCacheHits(long cacheHits) {
    this.cacheHits = cacheHits;
  }

  public long getCacheMisses() {
    return cacheMisses;
  }

  public void setCacheMisses(long cacheMisses) {
    this.cacheMisses = cacheMisses;
  }

  public long getNumObjectsInCache() {
    return numObjectsInCache;
  }

  public void setNumObjectsInCache(long numObjectsInCache) {
    this.numObjectsInCache = numObjectsInCache;
  }

  public long getMemoryUsed() {
    return memoryUsed;
  }

  public void setMemoryUsed(long memoryUsed) {
    this.memoryUsed = memoryUsed;
  }

  public long getNumObjectsOnDisk() {
    return numObjectsOnDisk;
  }

  public void setNumObjectsOnDisk(long numObjectsOnDisk) {
    this.numObjectsOnDisk = numObjectsOnDisk;
  }

}