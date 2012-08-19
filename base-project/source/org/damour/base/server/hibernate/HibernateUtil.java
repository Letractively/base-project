package org.damour.base.server.hibernate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.damour.base.client.objects.IHibernateFriendly;
import org.damour.base.client.utils.StringUtils;
import org.damour.base.server.BaseSystem;
import org.damour.base.server.Logger;
import org.damour.base.server.hibernate.helpers.DefaultData;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.DOMWriter;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
  private static HibernateUtil instance = null;

  private SessionFactory sessionFactory = null;
  private String username;
  private String password;
  private String connectString;

  private HashMap<Class<?>, Element> classElementMap = new HashMap<Class<?>, Element>();
  private HashMap<Class<?>, Element> idElementMap = new HashMap<Class<?>, Element>();
  private HashMap<Class<?>, Boolean> idElementClearedMap = new HashMap<Class<?>, Boolean>();
  private List<Class<?>> mappedClasses = new ArrayList<Class<?>>();
  private Document mappingDocument = DocumentHelper.createDocument();
  private Element mappingRoot = null;
  private String tablePrefix = "";
  private boolean showSQL = true;
  private boolean useGeneratedKeys = true;
  private boolean useReflectionOptimizer = true;
  private String hbm2ddlMode = "update";
  private String dialect = "org.hibernate.dialect.MySQL5InnoDBDialect";
  private boolean generateStatistics = true;

  private HibernateUtil(HashMap<String, String> overrides) {
    Logger.log("creating new HibernateUtil()");

    Properties rb = BaseSystem.getSettings();
    rb.putAll(overrides);

    setUsername(rb.getProperty("username"));
    setPassword(rb.getProperty("password"));
    setConnectString(rb.getProperty("connectString"));
    setTablePrefix(rb.getProperty("tablePrefix"));
    setHbm2ddlMode(getResource(rb, "hbm2ddlMode", "" + hbm2ddlMode));
    setDialect(getResource(rb, "dialect", "" + dialect));
    setShowSQL("true".equalsIgnoreCase(getResource(rb, "showSQL", "" + showSQL)));
    setUseGeneratedKeys("true".equalsIgnoreCase(getResource(rb, "useGeneratedKeys", "" + useGeneratedKeys)));
    setUseReflectionOptimizer("true".equalsIgnoreCase(getResource(rb, "useReflectionOptimizer", "" + useReflectionOptimizer)));
    setGenerateStatistics("true".equalsIgnoreCase(getResource(rb, "generateStatistics", "" + generateStatistics)));

    // add these mappings last because the settings above may affect them
    generateHibernateMappings(rb);
  }

  private void bootstrap() {
    try {
      org.hibernate.Session session = HibernateUtil.getInstance().getSession();
      Transaction tx = session.beginTransaction();
      try {
        IDefaultData defaultData = null;
        if (BaseSystem.getSettings().get("DefaultDataOverride") != null) {
          defaultData = (IDefaultData) Class.forName(BaseSystem.getSettings().getProperty("DefaultDataOverride")).newInstance();
        }
        if (defaultData == null) {
          defaultData = new DefaultData();
        }
        defaultData.create(session);
        tx.commit();
      } catch (HibernateException he) {
        tx.rollback();
        session.close();
      } finally {
        try {
          session.close();
        } catch (Throwable t) {
        }
      }
    } catch (Throwable t) {
      BaseSystem.setDomainName("sometests.com");
      Logger.log(t);
    }
  }

  private void generateHibernateMappings(Properties bundle) {
    try {
      Enumeration<?> keys = bundle.keys();
      while (keys.hasMoreElements()) {
        String key = (String) keys.nextElement();
        if (key.startsWith("HibernateMapped")) {
          try {
            Class<?> clazz = Class.forName(bundle.getProperty(key));
            generateHibernateMapping(clazz);
          } catch (Throwable t) {
            Logger.log(t);
          }
        }
      }
    } catch (Throwable t) {
      Logger.log(t);
    }
  }

  private String getResource(Properties bundle, String key, String defaultValue) {
    try {
      String property = bundle.getProperty(key);
      if (!StringUtils.isEmpty(property)) {
        return property;
      }
    } catch (Throwable t) {
    }
    return defaultValue;
  }

  public static synchronized HibernateUtil getInstance(HashMap<String, String> overrides) {
    if (instance == null) {
      instance = new HibernateUtil(overrides);
      instance.bootstrap();
      if (Logger.DEBUG) {
        Runnable r = new Runnable() {
          public void run() {
            int counter = 0;
            while (true) {
              try {
                // every minute
                Thread.sleep(60000);
              } catch (Exception e) {
              }              
              
              counter++;
              if (counter % 240 == 0) {
                // reset the hibernate cache every 4 hours
                HibernateUtil.resetHibernate();
              }
              
              System.gc();
              long total = Runtime.getRuntime().totalMemory();
              long free = Runtime.getRuntime().freeMemory();
              Logger.log(DecimalFormat.getNumberInstance().format(total) + " allocated " + DecimalFormat.getNumberInstance().format(total - free) + " used "
                  + DecimalFormat.getNumberInstance().format(free) + " free");
            }
          }
        };
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
      }
    }
    return instance;
  }

  public static synchronized HibernateUtil getInstance() {
    return getInstance(new HashMap<String, String>());
  }

  public static void resetHibernate() {
    try {
      instance.sessionFactory.getCurrentSession().close();
    } catch (Throwable t) {
    }
    try {
      instance.sessionFactory.close();
      instance.sessionFactory = null;
    } catch (Throwable t) {
    }
    instance = null;
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
      Logger.log(e);
    } catch (DocumentException e) {
      Logger.log(e);
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
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.jdbc.use_get_generated_keys").setText("" + getUseGeneratedKeys());
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.bytecode.use_reflection_optimizer")
            .setText("" + getUseReflectionOptimizer());
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.jdbc.batch_size").setText("25");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.dialect").setText(getDialect());
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.generate_statistics").setText("" + isGenerateStatistics());
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.cache.use_structured_entries").setText("true");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.cache.use_query_cache").setText("true");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.show_sql").setText("" + isShowSQL());
        // sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.format_sql").setText("" + showSQL);
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.jdbc.use_streams_for_binary").setText("true");

        // setup out provider for ehcache
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.cache.provider_class").setText("net.sf.ehcache.hibernate.SingletonEhCacheProvider");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.cache.region.factory_class").setText("net.sf.ehcache.hibernate.SingletonEhCacheRegionFactory");
        
        // add c3p0 configuration
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.connection.provider_class").setText("org.hibernate.connection.C3P0ConnectionProvider");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.c3p0.idle_test_period").setText("3600");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.c3p0.timeout").setText("7200");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.c3p0.min_size").setText("1");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.c3p0.max_size").setText("10");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.c3p0.max_statements").setText("0");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.c3p0.acquire_increment").setText("1");
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.c3p0.preferredTestQuery").setText("select 1+1");
        //sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.c3p0.testConnectionOnCheckout").setText("true");
        
        // generate ddl and update database (if configured)
        sessionFactoryElement.addElement("property").addAttribute("name", "hibernate.hbm2ddl.auto").setText(hbm2ddlMode);
        // setup config
        Configuration cfg = new Configuration().configure(new DOMWriter().write(document));
        // add object mappings
        Logger.log(mappingDocument.asXML());
        cfg.addDocument(new DOMWriter().write(mappingDocument));
        sessionFactory = cfg.buildSessionFactory();
      } catch (Exception e) {
        Logger.log(e);
      }
    }
    return sessionFactory;
  }

  private void addMappedClass(Class<?> clazz) {
    mappedClasses.add(clazz);
  }

  private boolean isClassMapped(Class<?> clazz) {
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

  public List<?> executeQuery(Session session, String query, boolean cacheResults, int maxResults) {
    Logger.log(query);
    Query q = session.createQuery(query).setCacheable(cacheResults).setMaxResults(maxResults);
    return q.list();
  }

  @SuppressWarnings("rawtypes")
  public List executeQuery(Session session, String query, boolean cacheResults) {
    Logger.log(query);
    Query q = session.createQuery(query).setCacheable(cacheResults);
    return q.list();
  }

  @SuppressWarnings({ "rawtypes" })
  public List executeQuery(Session session, String query) {
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

  public void generateHibernateMapping(Class<?> clazz) {
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
        keyElement.addAttribute("column", "id");
        idElementMap.put(clazz, keyElement);
      } else {
        mappingElement = mappingRootElement.addElement("class");
        mappingElement.addAttribute("name", clazz.getName());
        mappingElement.addAttribute("table", getTablePrefix() + clazz.getSimpleName().toLowerCase());
        // id
        Element keyElement = mappingElement.addElement("id");
        keyElement.addAttribute("name", "id");
        keyElement.addAttribute("type", "long");
        keyElement.addAttribute("column", "id");
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

      List<Field> fields = ReflectionCache.getFields(clazz);
      for (Field field : fields) {

        String name = field.getName();

        // check if the field is hibernate managed
        try {
          Method isFieldMappedMethod = clazz.getMethod("isFieldMapped", String.class);
          boolean isFieldMapped = (Boolean) isFieldMappedMethod.invoke(clazz.newInstance(), name);
          if (!isFieldMapped) {
            // skip it
            Logger.log("  -" + name + ":" + field.getType().getName());
            continue;
          }
        } catch (Throwable t) {
          Logger.log("Cannot determine if field is hibernated managed:" + field.getName() + " (" + field.getType().getName() + ")");
        }

        boolean skip = false;
        List<Field> parentFields = ReflectionCache.getFields(clazz.getSuperclass());
        for (Field parentField : parentFields) {
          if (field.equals(parentField)) {
            // skip duplicates
            // Logger.log("  -" + name + ":" + field.getType().getName());
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
        String typeOverride = null;
        int fieldLength = -1;
        if (IHibernateFriendly.class.isAssignableFrom(clazz)) {
          try {
            Method isKeyMethod = clazz.getMethod("isFieldKey", String.class);
            Method isUniqueMethod = clazz.getMethod("isFieldUnique", String.class);
            Method getFieldTypeMethod = clazz.getMethod("getFieldType", String.class);
            Method getFieldLengthMethod = clazz.getMethod("getFieldLength", String.class);
            isKey = (Boolean) isKeyMethod.invoke(clazz.newInstance(), name);
            isUnique = (Boolean) isUniqueMethod.invoke(clazz.newInstance(), name);
            typeOverride = (String) getFieldTypeMethod.invoke(clazz.newInstance(), name);
            fieldLength = (Integer) getFieldLengthMethod.invoke(clazz.newInstance(), name);
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
            keyEntry.addAttribute("column", field.getName());
          } else {
            Element keyEntry = keyElement.addElement("key-property");
            keyEntry.addAttribute("name", field.getName());
            keyEntry.addAttribute("column", field.getName());
          }
          continue;
        }

        if (!name.equals("id")) {
          // some types might be special
          String type = field.getType().getSimpleName().toLowerCase();
          if (isJavaType(field.getType())) {
            Element propertyElement = mappingElement.addElement("property");
            propertyElement.addAttribute("name", name);
            if (typeOverride != null) {
              propertyElement.addAttribute("type", typeOverride);
            } else {
              propertyElement.addAttribute("type", type);
            }
            if (fieldLength > 0) {
              propertyElement.addAttribute("length", "" + fieldLength);
            }
            propertyElement.addAttribute("column", name);
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
            setElement.addElement("key").addAttribute("column", "id");
            setElement.addElement("one-to-many").addAttribute("class", ((Class<?>) genericType.getActualTypeArguments()[0]).getName());
          } else if (byte[].class.equals(field.getType())) {
            Element propertyElement = mappingElement.addElement("property");
            propertyElement.addAttribute("name", name);
            propertyElement.addAttribute("type", "binary");
            // BinaryBlobType.class.getName()
            // propertyElement.addAttribute("column", getColumnPrefix() + name);
            propertyElement.addElement("column").addAttribute("name", name).addAttribute("sql-type", "LONGBLOB");
            if (isUnique) {
              propertyElement.addAttribute("unique", "true");
            }
          } else {
            Element manyToOneElement = mappingElement.addElement("many-to-one");
            manyToOneElement.addAttribute("name", name);
            manyToOneElement.addAttribute("class", field.getType().getName());
            manyToOneElement.addAttribute("column", name);
            manyToOneElement.addAttribute("lazy", "false");

            if (isUnique) {
              manyToOneElement.addAttribute("unique", "true");
            }
          }
        }
      }
      addMappedClass(clazz);
      Logger.log("Finished mapping for " + clazz.getSimpleName().toLowerCase());
    }
  }

  private boolean isJavaType(Class<?> clazz) {
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

  private void setTablePrefix(String tablePrefix) {
    this.tablePrefix = tablePrefix;
  }

  public String getHbm2ddlMode() {
    return hbm2ddlMode;
  }

  private void setHbm2ddlMode(String hbm2ddlMode) {
    this.hbm2ddlMode = hbm2ddlMode;
  }

  public boolean isShowSQL() {
    return showSQL;
  }

  private void setShowSQL(boolean showSQL) {
    this.showSQL = showSQL;
  }

  public String getDialect() {
    return dialect;
  }

  public void setDialect(String dialect) {
    this.dialect = dialect;
  }

  public boolean getUseGeneratedKeys() {
    return useGeneratedKeys;
  }

  public void setUseGeneratedKeys(boolean useGeneratedKeys) {
    this.useGeneratedKeys = useGeneratedKeys;
  }

  public boolean getUseReflectionOptimizer() {
    return useReflectionOptimizer;
  }

  public void setUseReflectionOptimizer(boolean useReflectionOptimizer) {
    this.useReflectionOptimizer = useReflectionOptimizer;
  }

  public boolean isGenerateStatistics() {
    return generateStatistics;
  }

  public void setGenerateStatistics(boolean generateStatistics) {
    this.generateStatistics = generateStatistics;
  }

  public void printStatistics() {
    Session session = HibernateUtil.getInstance().getSession();
    // org.hibernate.Session session = HibernateUtil.getInstance().getSession();
    System.out.println("Query Cache: p" + session.getSessionFactory().getStatistics().getQueryCachePutCount() + " h"
        + session.getSessionFactory().getStatistics().getQueryCacheHitCount() + " m" + session.getSessionFactory().getStatistics().getQueryCacheMissCount());
    System.out.println("2nd Level Cache: p" + session.getSessionFactory().getStatistics().getSecondLevelCachePutCount() + " h"
        + session.getSessionFactory().getStatistics().getSecondLevelCacheHitCount() + " m"
        + session.getSessionFactory().getStatistics().getSecondLevelCacheMissCount());
  }

}
