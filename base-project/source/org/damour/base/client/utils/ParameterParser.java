package org.damour.base.client.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.http.client.URL;

public class ParameterParser {

  private String queryString;
  private Map<String, String> paramMap;
  private Map<String, List<String>> listParamMap;
  private List<String> orderedParameterNames = new ArrayList<String>();

  public static String convertRESTtoQueryString(String restURL) {
    // convert stuff like this:
    // /view/take-test/id/12345/name/Test-Name-1.html
    // to this:
    // ?view=take-test&id=12345&name=Test-Name-1.html
    String queryString = "";
    if (!StringUtils.isEmpty(restURL)) {
      StringTokenizer st = new StringTokenizer(restURL, "/");
      int numTokens = st.countTokens();
      for (int i = 0; i < numTokens;) {
        if (i + 1 < numTokens) {
          queryString += ((i == 0) ? "?" : "&") + st.tokenAt(i) + "=" + st.tokenAt(i + 1);
        }
        i += 2;
      }
    }
    return queryString;
  }

  public ParameterParser(String queryString) {
    if (queryString != null) {
      if (queryString.startsWith("#") || queryString.startsWith("?")) {
        queryString = queryString.substring(1);
      }
    }
    this.queryString = queryString;
  }

  private void ensureParameterMap() {
    if (paramMap == null) {
      paramMap = new HashMap<String, String>();
      if (queryString != null && queryString.length() > 1) {
        for (String kvPair : queryString.split("&")) {
          String[] kv = kvPair.split("=", 2);
          orderedParameterNames.add(kv[0]);
          if (kv.length > 1) {
            paramMap.put(kv[0], URL.decodeQueryString(kv[1]));
          } else {
            paramMap.put(kv[0], "");
          }
        }
      }
    }
  }

  /**
   * Builds the immutable map from String to List<String> that we'll return in getParameterMap(). Package-protected for testing.
   * 
   * @return a map from the
   */
  private Map<String, List<String>> buildListParamMap(String queryString) {
    Map<String, List<String>> out = new HashMap<String, List<String>>();

    if (queryString != null && queryString.length() > 1) {
      String qs = queryString;
      if (queryString.startsWith("&") || queryString.startsWith("?") || queryString.startsWith("#")) {
        qs = queryString.substring(1);
      }

      for (String kvPair : qs.split("&")) {
        String[] kv = kvPair.split("=", 2);
        if (kv[0].length() == 0) {
          continue;
        }

        List<String> values = out.get(kv[0]);
        if (values == null) {
          values = new ArrayList<String>();
          out.put(kv[0], values);
        }
        values.add(kv.length > 1 ? URL.decodeQueryString(kv[1]) : "");
      }
    }

    for (Map.Entry<String, List<String>> entry : out.entrySet()) {
      entry.setValue(Collections.unmodifiableList(entry.getValue()));
    }

    out = Collections.unmodifiableMap(out);

    return out;
  }

  /**
   * Gets the URL's parameter of the specified name. Note that if multiple parameters have been specified with the same name, the last one will be returned.
   * 
   * @param name
   *          the name of the URL's parameter
   * @return the value of the URL's parameter
   */
  public String getParameter(String name) {
    ensureParameterMap();
    return paramMap.get(name);
  }

  /**
   * Returns a Map of the URL query parameters for the host page; since changing the map would not change the window's location, the map returned is immutable.
   * 
   * @return a map from URL query parameter names to the value
   */
  public Map<String, String> getParameterMap() {
    ensureParameterMap();
    return paramMap;
  }

  /**
   * Returns a Map of the URL query parameters for the host page; since changing the map would not change the window's location, the map returned is immutable.
   * 
   * @return a map from URL query parameter names to a list of values
   */
  public Map<String, List<String>> getListParameterMap() {
    if (listParamMap == null) {
      listParamMap = buildListParamMap(queryString);
    }
    return listParamMap;
  }

  public List<String> getParameterValues(String name) {
    return getListParameterMap().get(name);
  }

  public List<String> getOrderedParameterNames() {
    ensureParameterMap();
    return orderedParameterNames;
  }

  public void setOrderedParameterNames(List<String> orderedParameterNames) {
    this.orderedParameterNames = orderedParameterNames;
  }

  public static native String getMetaTag(String name)
  /*-{
    var m = $doc.getElementsByTagName('meta'); 
    for(var i in m) { 
      if(m[i].name == name) {
        return m[i].content;
      } 
    }
    return null;
  }-*/;
  
}