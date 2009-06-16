package org.damour.base.server.hibernate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReflectionCache {

  private static HashMap<Class, List<Field>> classFieldMap = new HashMap<Class, List<Field>>();

  public static List<Field> getFields(Class clazz) {
    List<Field> fields = classFieldMap.get(clazz);
    if (fields == null) {
      fields = new ArrayList<Field>();
      Field fieldArray[] = clazz.getFields();
      for (Field field : fieldArray) {
        if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
          fields.add(field);
        }
      }
      classFieldMap.put(clazz, fields);
    }
    return fields;
  }

}
