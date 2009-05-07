package org.damour.base.client.objects;

public interface IHibernateFriendly {
  public boolean isFieldUnique(String fieldName);
  public boolean isFieldKey(String fieldName);
  public String getSqlUpdate();
  public String getCachePolicy();
  public boolean isLazy();
  public boolean isFieldMapped(String fieldName);
}
