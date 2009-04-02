package org.damour.base.server.hibernate;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ReflectionCache {

  private static HashMap<Class,Field[]> classFieldMap = new HashMap<Class, Field[]>();
  
  public static Field[] getFields(Class clazz) {
    Field fields[] = classFieldMap.get(clazz);
    if (fields == null) {
      fields = clazz.getFields();
      classFieldMap.put(clazz, fields);
    }
    return fields;
  }

}
