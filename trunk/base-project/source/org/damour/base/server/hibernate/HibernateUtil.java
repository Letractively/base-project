package org.damour.base.server.hibernate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.damour.base.client.Logger;
import org.damour.base.client.objects.IHibernateFriendly;
import org.damour.base.server.ReflectionCache;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.DOMWriter;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
  public static final boolean DEBUG = true;
  public static final long startupDate = System.currentTimeMillis();

  protected static HibernateUtil instance = null;

  private SessionFactory sessionFactory = null;
  private String username;
  private String password;
  private String connectString;

  private List<Class> mappedClasses = new ArrayList<Class>();
  private Document mappingDocument = DocumentHelper.createDocument();
  private Element mappingRoot = null;
  private String tablePrefix = "";
  private String columnPrefix = "";
  private boolean showSQL = true;
  private String hbm2ddlMode = "update";

  public HibernateUtil() {
    if (DEBUG) {
      // test
      setUsername("root");
      setPassword("t@k30ff");
      setConnectString("jdbc:mysql://localhost/agedicator?autoReconnect=true&blobSendChunkSize=256000");

      Runnable r = new Runnable() {
        public void run() {
          while (true) {
            System.gc();
            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();
            System.out.println(DecimalFormat.getNumberInstance().format(total) + " allocated " + DecimalFormat.getNumberInstance().format(total - free) + " used " + DecimalFormat.getNumberInstance().format(free) + " free");
            try {
              Thread.sleep(30000);
            } catch (Exception e) {
            }
          }
        }
      };
      Thread t = new Thread(r);
      t.setDaemon(true);
      t.start();

    } else {
      // production
      setUsername("%username%");
      setPassword("%password%");
      setConnectString("%connectString%");
    }
  }

  public static HibernateUtil getInstance() {
    if (instance == null) {
      instance = new HibernateUtil();
    }
    return instance;
  }

  public void resetHibernate() {
    try {
      sessionFactory.close();
    } catch (Throwable t) {
    }
    sessionFactory = null;
  }

  public void setSessionFactory(SessionFactory inSessionFactory) {
    sessionFactory = inSessionFactory;
  }

  public SessionFactory getSessionFactory(Document configurationDocument) {
    Configuration cfg;
    try {
      cfg = new Configuration().configure(new DOMWriter().write(configurationDocument));
      sessionFactory = cfg.buildSessionFactory();
    } catch (HibernateException e) {
      e.printStackTrace();
    } catch (DocumentException e) {
      e.printStackTrace();
    }
    return sessionFactory;
  }

  public SessionFactory getSessionFactory() {
    if (sessionFactory == null) {
      try {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("hibernate-configuration");
        Element sessionFactoryElement = root.addElement("session-factory");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.connection.driver_class").setText("com.mysql.jdbc.Driver");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.connection.username").setText(getUsername());
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.connection.password").setText(getPassword());
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.connection.url").setText(getConnectString());
        // this property prevents a performance enhancement, but on godaddy.com they do not allow reflection created methods, as this would end up creating
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.jdbc.use_get_generated_keys").setText("false");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.jdbc.batch_size").setText("25");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.dialect").setText("org.hibernate.dialect.MySQLInnoDBDialect");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.bytecode.use_reflection_optimizer").setText("false");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.generate_statistics").setText("true");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.cache.use_structured_entries").setText("true");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.cache.use_query_cache").setText("true");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.show_sql").setText("" + showSQL);
        //sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.format_sql").setText("" + showSQL);
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.jdbc.use_streams_for_binary").setText("true");

        // setup out provider for ehcache
        sessionFactoryElement.addElement("property").addAttribute("name", "cache.provider_class").setText(CacheProvider.class.getName());
        // add c3p0 configuration
        sessionFactoryElement.addElement("property").addAttribute("name", "c3p0.acquire_increment").setText("1");
        sessionFactoryElement.addElement("property").addAttribute("name", "c3p0.idle_test_period").setText("60");
        sessionFactoryElement.addElement("property").addAttribute("name", "c3p0.timeout").setText("120");
        sessionFactoryElement.addElement("property").addAttribute("name", "c3p0.max_size").setText("5");
        sessionFactoryElement.addElement("property").addAttribute("name", "c3p0.max_statements").setText("0");
        sessionFactoryElement.addElement("property").addAttribute("name", "c3p0.min_size").setText("1");
        sessionFactoryElement.addElement("property").addAttribute("name", "c3p0.preferredTestQuery").setText("select 1+1");
        // generate ddl and update database
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.hbm2ddl.auto").setText(hbm2ddlMode);
        // setup config
        Configuration cfg = new Configuration().configure(new DOMWriter().write(document));
        // add object mappings
        Logger.log(mappingDocument.asXML());
        cfg.addDocument(new DOMWriter().write(mappingDocument));
        sessionFactory = cfg.buildSessionFactory();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return sessionFactory;
  }

  private void addMappedClass(Class clazz) {
    mappedClasses.add(clazz);
  }

  private boolean isClassMapped(Class clazz) {
    return mappedClasses.contains(clazz);
  }

  private Element getMappingElement() {
    if (mappingRoot == null) {
      mappingRoot = mappingDocument.addElement("hibernate-mapping");
      // mappingRoot.addAttribute("default-lazy", "true");
      // mappingRoot.addAttribute("default-cascade", "all,delete-orphan");
    }
    return mappingRoot;
  }

  public synchronized Session getSession() {
    return getSessionFactory().openSession();
  }

  public List executeQuery(Session session, String query, boolean cacheResults, int maxResults) {
    Logger.log(query);
    Query q = session.createQuery(query).setCacheable(cacheResults).setMaxResults(maxResults);
    return q.list();
  }

  public List executeQuery(Session session, String query, boolean cacheResults) {
    Logger.log(query);
    Query q = session.createQuery(query).setCacheable(cacheResults);
    return q.list();
  }

  public List executeQuery(Session session, String query) {
    Logger.log(query);
    return executeQuery(session, query, true);
  }

  private org.safehaus.uuid.UUIDGenerator guidGenerator = org.safehaus.uuid.UUIDGenerator.getInstance();

  public String generateGUID() {
    return guidGenerator.generateTimeBasedUUID().toString();
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    sessionFactory = null;
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    sessionFactory = null;
    this.password = password;
  }

  public String getConnectString() {
    return connectString;
  }

  public void setConnectString(String connectString) {
    sessionFactory = null;
    this.connectString = connectString;
  }

  public long getStartupDate() {
    return startupDate;
  }

  HashMap<Class, Element> classElementMap = new HashMap<Class, Element>();
  HashMap<Class, Element> idElementMap = new HashMap<Class, Element>();
  HashMap<Class, Boolean> idElementClearedMap = new HashMap<Class, Boolean>();

  public void generateHibernateMapping(Class clazz) {
    if (!isClassMapped(clazz)) {
      Logger.log("Adding mapping for " + clazz.getSimpleName().toLowerCase());
      Element mappingRootElement = getMappingElement();
      Element mappingElement = null;

      // check parent superclass, if it is not Object, then add it's mapping
      if (!clazz.getSuperclass().equals(Object.class)) {
        generateHibernateMapping(clazz.getSuperclass());
        Element parentMappingElement = classElementMap.get(clazz.getSuperclass());
        // 9.1.2. Table per subclass
        mappingElement = parentMappingElement.addElement("joined-subclass");
        mappingElement.addAttribute("name", clazz.getName());
        mappingElement.addAttribute("table", getTablePrefix() + clazz.getSimpleName().toLowerCase());
        // key
        Element keyElement = mappingElement.addElement("key");
        keyElement.addAttribute("column", getColumnPrefix() + "id");
        idElementMap.put(clazz, keyElement);
      } else {
        mappingElement = mappingRootElement.addElement("class");
        mappingElement.addAttribute("name", clazz.getName());
        mappingElement.addAttribute("table", getTablePrefix() + clazz.getSimpleName().toLowerCase());
        // id
        Element keyElement = mappingElement.addElement("id");
        keyElement.addAttribute("name", "id");
        keyElement.addAttribute("type", "long");
        keyElement.addAttribute("column", getColumnPrefix() + "id");
        // generator
        Element generatorElement = keyElement.addElement("generator");
        generatorElement.addAttribute("class", "native");
        idElementMap.put(clazz, keyElement);
      }
      // add class / mappingElement to list (so we don't do it again)
      classElementMap.put(clazz, mappingElement);

      String sqlUpdate = null;
      String cachePolicy = "none";
      boolean lazy = true;
      if (IHibernateFriendly.class.isAssignableFrom(clazz)) {
        try {
          Method getSqlUpdate = clazz.getMethod("getSqlUpdate");
          sqlUpdate = (String) getSqlUpdate.invoke(clazz.newInstance());
          Method getCachePolicy = clazz.getMethod("getCachePolicy");
          Method isLazy = clazz.getMethod("isLazy");
          cachePolicy = (String) getCachePolicy.invoke(clazz.newInstance());
          lazy = (Boolean) isLazy.invoke(clazz.newInstance());
        } catch (Exception e) {
        }
      }
      if (sqlUpdate != null && !"".equals(sqlUpdate)) {
        mappingElement.addElement("sql-update").setText(sqlUpdate);
      }
      if (cachePolicy != null && !"".equals(cachePolicy) && !"none".equals(cachePolicy)) {
        // cache usage
        Element cacheElement = mappingElement.addElement("cache");
        cacheElement.addAttribute("usage", cachePolicy);
      }
      mappingElement.addAttribute("lazy", "" + lazy);

      Field[] fields = ReflectionCache.getFields(clazz);
      for (Field field : fields) {

        String name = field.getName();

        boolean skip = false;
        Field[] parentFields = ReflectionCache.getFields(clazz.getSuperclass());
        for (Field parentField : parentFields) {
          if (field.equals(parentField)) {
            // skip duplicates
            Logger.log("  -" + name + ":" + field.getType().getName());
            skip = true;
            break;
          }
        }

        if (skip) {
          continue;
        }
        Logger.log("  +" + name + ":" + field.getType().getName());

        Boolean isKey = Boolean.FALSE;
        Boolean isUnique = Boolean.FALSE;
        if (IHibernateFriendly.class.isAssignableFrom(clazz)) {
          try {
            Method isKeyMethod = clazz.getMethod("isFieldKey", String.class);
            Method isUniqueMethod = clazz.getMethod("isFieldUnique", String.class);
            isKey = (Boolean) isKeyMethod.invoke(clazz.newInstance(), name);
            isUnique = (Boolean) isUniqueMethod.invoke(clazz.newInstance(), name);
          } catch (Exception e) {
          }
        }
        if (isKey) {
          Element keyElement = idElementMap.get(clazz);
          if (idElementClearedMap.get(clazz) == null || !idElementClearedMap.get(clazz)) {
            keyElement.detach();
            keyElement = mappingElement.addElement("composite-id");
            idElementMap.put(clazz, keyElement);
            idElementClearedMap.put(clazz, Boolean.TRUE);
          }
          if (!isJavaType(field.getType())) {
            Element keyEntry = keyElement.addElement("key-many-to-one");
            keyEntry.addAttribute("name", field.getName());
            keyEntry.addAttribute("class", field.getType().getName());
            keyEntry.addAttribute("column", getColumnPrefix() + field.getName());
          } else {
            Element keyEntry = keyElement.addElement("key-property");
            keyEntry.addAttribute("name", field.getName());
            keyEntry.addAttribute("column", getColumnPrefix() + field.getName());
          }
          continue;
        }

        if (!name.equals("id")) {
          // some types might be special
          String type = field.getType().getSimpleName().toLowerCase();
          if (isJavaType(field.getType())) {
            Element propertyElement = mappingElement.addElement("property");
            propertyElement.addAttribute("name", name);
            propertyElement.addAttribute("type", type);
            propertyElement.addAttribute("column", getColumnPrefix() + name);
            if (isUnique) {
              propertyElement.addAttribute("unique", "true");
            }
          } else if (field.getType().isAssignableFrom(Set.class)) {
            // add set
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();

            Element setElement = mappingElement.addElement("set");
            setElement.addAttribute("name", name);
            setElement.addElement("cache").addAttribute("usage", "nonstrict-read-write");
            setElement.addAttribute("inverse", "true");
            setElement.addAttribute("lazy", "false");
            // setElement.addAttribute("cascade", "all-delete-orphan");
            setElement.addElement("key").addAttribute("column", getColumnPrefix() + "id");
            setElement.addElement("one-to-many").addAttribute("class", ((Class) genericType.getActualTypeArguments()[0]).getName());
          } else if (byte[].class.equals(field.getType())) {
            Element propertyElement = mappingElement.addElement("property");
            propertyElement.addAttribute("name", name);
            propertyElement.addAttribute("type", "binary");
            // BinaryBlobType.class.getName()
            // propertyElement.addAttribute("column", getColumnPrefix() + name);
            propertyElement.addElement("column").addAttribute("name", getColumnPrefix() + name).addAttribute("sql-type", "LONGBLOB");
            if (isUnique) {
              propertyElement.addAttribute("unique", "true");
            }
          } else {
            Element manyToOneElement = mappingElement.addElement("many-to-one");
            manyToOneElement.addAttribute("name", name);
            manyToOneElement.addAttribute("class", field.getType().getName());
            manyToOneElement.addAttribute("column", getColumnPrefix() + name);
            manyToOneElement.addAttribute("lazy", "false");

            if (isUnique) {
              manyToOneElement.addAttribute("unique", "true");
            }
          }
        }
      }
      addMappedClass(clazz);
      resetHibernate();
      Logger.log("Finished mapping for " + clazz.getSimpleName().toLowerCase());
    }
  }

  private boolean isJavaType(Class clazz) {
    if (clazz.isPrimitive()) {
      return true;
    } else if (Boolean.class.isAssignableFrom(clazz)) {
      return true;
    } else if (Number.class.isAssignableFrom(clazz)) {
      return true;
    } else if (String.class.isAssignableFrom(clazz)) {
      return true;
    }
    return false;
  }

  public String getTablePrefix() {
    return tablePrefix;
  }

  public void setTablePrefix(String tablePrefix) {
    this.tablePrefix = tablePrefix;
  }

  public String getColumnPrefix() {
    return columnPrefix;
  }

  public void setColumnPrefix(String columnPrefix) {
    this.columnPrefix = columnPrefix;
  }

  public String getHbm2ddlMode() {
    return hbm2ddlMode;
  }

  public void setHbm2ddlMode(String hbm2ddlMode) {
    this.hbm2ddlMode = hbm2ddlMode;
  }

  public void printStatistics() {
    Session session = HibernateUtil.getInstance().getSession();
    // org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    System.out.println("Query Cache: p" + session.getSessionFactory().getStatistics().getQueryCachePutCount() + " h" + session.getSessionFactory().getStatistics().getQueryCacheHitCount() + " m"
        + session.getSessionFactory().getStatistics().getQueryCacheMissCount());
    System.out.println("2nd Level Cache: p" + session.getSessionFactory().getStatistics().getSecondLevelCachePutCount() + " h" + session.getSessionFactory().getStatistics().getSecondLevelCacheHitCount() + " m"
        + session.getSessionFactory().getStatistics().getSecondLevelCacheMissCount());
  }

  public boolean isShowSQL() {
    return showSQL;
  }

  public void setShowSQL(boolean showSQL) {
    this.showSQL = showSQL;
  }

}
