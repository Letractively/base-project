/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.damour.base.client.utils;

import com.google.gwt.http.client.URL;

// see: http://commons.apache.org/lang/apidocs/org/apache/commons/lang/StringUtils.html

public class StringUtils {

  public static boolean isEmpty(String str) {
    return null == str || "".equals(str.trim()); //$NON-NLS-1$
  }

  public static String defaultIfEmpty(String str, String xdefault) {
    return StringUtils.isEmpty(str) ? xdefault : str;
  }

  public static String addStringToInt(String strAddend, int addend) {
    return Integer.toString(Integer.parseInt(strAddend) + addend);
  }

  public static String multiplyStringWithInt(String strMultiplicand, int multiplier) {
    return Integer.toString(Integer.parseInt(strMultiplicand) * multiplier);
  }

  public static String divideStringWithInt(String strDividend, int divisor) {
    return Integer.toString(Integer.parseInt(strDividend) / divisor);
  }

  public static String patchURL(String unsafeURL) {
    if (unsafeURL == null) {
      return null;
    }
    unsafeURL = unsafeURL.replaceAll("\"", "").replaceAll("\'", "").replaceAll("\\?", "-").replaceAll(" ", "-");
    unsafeURL = unsafeURL.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\[", "").replaceAll("\\]", "");
    unsafeURL = unsafeURL.replaceAll("\\$", "").replaceAll("\\@", "-").replaceAll("\\!", "-").replaceAll("\\&", "-");
    unsafeURL = unsafeURL.replaceAll("\\+", "-").replaceAll("\\<", "").replaceAll("\\>", "").replaceAll("/", "-");
    unsafeURL = unsafeURL.replaceAll(":", "-").replaceAll("\\{", "").replaceAll("\\}", "").replaceAll(",", "-");
    unsafeURL = unsafeURL.replaceAll(";", "-").replaceAll("`", "");
    while (unsafeURL.contains("--")) {
      unsafeURL = unsafeURL.replaceAll("--", "-");
    }

    return URL.encode(unsafeURL);
  }

  // MAX_INT = 2147483647
  private static final String MATCH_POSITIVE_INTEGER_RE = "^\\s*[0-9]{1,10}\\s*$"; //$NON-NLS-1$

  public static boolean isPositiveInteger(String strInt) {
    int length = strInt.length();
    if (length > 10) {
      return false; // MAX_INT has 10 digits
    } else if (length == 10) {
      // This is an execution-expensive path, since it relies on exception
      // processing to determine if strInt is an Integer or not. Minimize
      // the number of times this else-if is entered
      if (strInt.matches(MATCH_POSITIVE_INTEGER_RE)) {
        try {
          Integer.parseInt(strInt);
          return true;
        } catch (NumberFormatException ignore) {
        }
      }
      return false;
    } else {
      return strInt.matches(MATCH_POSITIVE_INTEGER_RE);
    }
  }
}
