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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Marshalling context which uses new commons pool2 for pooling of non
 * validating data binding instances.
 * 
 * @author Scott Cinnamond
 * @since 0.6.2
 * 
 * @see StateNonValidatingDataBinding
 * @see StateMarshalingContext
 */
public class PooledStateMarshallingContext implements StateMarshalingContext {
  private static Log log = LogFactory.getLog(PooledStateMarshallingContext.class);
  private GenericObjectPool<StateNonValidatingDataBinding> pool;

  @SuppressWarnings("unused")
  private PooledStateMarshallingContext() {
  }

  public PooledStateMarshallingContext(GenericObjectPoolConfig config,
      StateDataBindingFactory factory) {
    if (log.isDebugEnabled())
      log.debug("initializing data binding pool...");
    this.pool = new GenericObjectPool<StateNonValidatingDataBinding>(factory);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.cloudgraph.state.StateMarshalingContext#getBinding()
   */
  @Override
  public NonValidatingDataBinding getBinding() {
    try {
      return this.pool.borrowObject();
    } catch (Exception e) {
      throw new StateException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.cloudgraph.state.StateMarshalingContext#returnDataBinding(org.cloudgraph
   * .state.NonValidatingDataBinding)
   */
  @Override
  public void returnBinding(NonValidatingDataBinding binding) {
    this.pool.returnObject((StateNonValidatingDataBinding) binding);
  }

}
