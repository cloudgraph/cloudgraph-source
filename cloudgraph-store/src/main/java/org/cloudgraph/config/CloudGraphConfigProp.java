/**
 * Copyright 2017 TerraMeta Software, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cloudgraph.config;

import org.plasma.query.Query;

public class CloudGraphConfigProp {

  public static QueryFetchType getQueryFetchType(Query query) {
    QueryFetchType fetchType = QueryFetchType.SERIAL;
    String fetchTypeValue = System.getProperty(ConfigurationProperty.CLOUDGRAPH___QUERY___FETCHTYPE
        .value());
    if (fetchTypeValue != null)
      try {
        fetchType = QueryFetchType.fromValue(fetchTypeValue);
      } catch (IllegalArgumentException e) {
        throw new CloudGraphConfigurationException("unknown query configuration value '"
            + fetchTypeValue + "' for property, "
            + ConfigurationProperty.CLOUDGRAPH___QUERY___FETCHTYPE.value(), e);
      }
    // override it with query specific value
    fetchTypeValue = query
        .getConfigurationProperty(ConfigurationProperty.CLOUDGRAPH___QUERY___FETCHTYPE.value());
    if (fetchTypeValue != null)
      try {
        fetchType = QueryFetchType.fromValue(fetchTypeValue);
      } catch (IllegalArgumentException e) {
        throw new CloudGraphConfigurationException("unknown query configuration value '"
            + fetchTypeValue + "' for property, "
            + ConfigurationProperty.CLOUDGRAPH___QUERY___FETCHTYPE.value(), e);
      }
    return fetchType;
  }

  public static int getQueryPoolMin(Query query) {
    int minPool = findIntValue(query,
        ConfigurationProperty.CLOUDGRAPH___QUERY___THREADPOOL___SIZE___MIN.value(), 10);
    return minPool;
  }

  public static int getQueryPoolMax(Query query) {
    int maxPool = findIntValue(query,
        ConfigurationProperty.CLOUDGRAPH___QUERY___THREADPOOL___SIZE___MAX.value(), 10);
    return maxPool;
  }

  public static int getQueryThreadMaxDepth(Query query) {
    int depthMax = findIntValue(query,
        ConfigurationProperty.CLOUDGRAPH___QUERY___THREAD___DEPTH___MAX.value(), 3);
    return depthMax;
  }

  private static int findIntValue(Query query, String propertyName, int dflt) {
    int intValue = dflt;
    String value = System.getProperty(propertyName);
    if (value != null)
      try {
        intValue = Integer.valueOf(value);
      } catch (NumberFormatException nfe) {
        throw new CloudGraphConfigurationException("invalid system query configuration value '"
            + value + "' for property, " + propertyName, nfe);
      }
    // override it with query specific value
    String stringValue = query.getConfigurationProperty(propertyName);
    if (stringValue != null)
      try {
        intValue = Integer.valueOf(stringValue);
      } catch (NumberFormatException nfe) {
        throw new CloudGraphConfigurationException("invalid query configuration value '"
            + stringValue + "' for property, " + propertyName, nfe);
      }
    return intValue;
  }

}
