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
package org.cloudgraph.state;

import org.cloudgraph.store.mapping.StoreMappingContext;
import org.cloudgraph.store.mapping.TableMapping;

/**
 * Provides access to the configuration and state related context information
 * for a specific table.
 * 
 * @see org.cloudgraph.store.mapping.TableMapping
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public interface TableState {

  /**
   * Returns the table configuration for this context.
   * 
   * @return the table configuration for this context.
   */
  public TableMapping getTableConfig();

  public StoreMappingContext getMappingContext();
}
