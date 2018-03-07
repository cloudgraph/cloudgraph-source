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
package org.cloudgraph.store.mapping;

import java.nio.charset.Charset;

import org.cloudgraph.store.mapping.codec.HashingKeyFieldCodec;
import org.cloudgraph.store.mapping.codec.KeyFieldCodec;
import org.cloudgraph.store.mapping.codec.LexicographicHashingKeyFieldCodec;
import org.cloudgraph.store.mapping.codec.MinBytesKeyFieldCodec;
import org.cloudgraph.store.mapping.codec.NativeKeyFieldCodec;
import org.cloudgraph.store.mapping.codec.PaddingKeyFieldCodec;
import org.plasma.sdo.DataFlavor;
import org.plasma.sdo.DataType;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.core.CoreConstants;

import commonj.sdo.DataObject;

/**
 * The configuration for a row or column key.
 * 
 * @author Scott Cinnamond
 * @since 0.5.1
 */
public abstract class KeyFieldMapping {
  protected int sequenceNum;
  /** the total number of fields in the row or column composite key */
  protected int totalFields;
  public static Charset CHARSET = Charset.forName(CoreConstants.UTF8_ENCODING);
  protected DataGraphMapping dataGraph;
  private KeyField field;
  protected KeyFieldCodec keyFieldWriter;

  @SuppressWarnings("unused")
  private KeyFieldMapping() {
  }

  public KeyFieldMapping(DataGraphMapping dataGraph, KeyField field, int sequenceNum,
      int totalFields) {
    super();
    this.dataGraph = dataGraph;
    this.field = field;
    this.sequenceNum = sequenceNum;
    this.totalFields = totalFields;
  }

  public int getSeqNum() {
    return sequenceNum;
  }

  public int getTotalFields() {
    return totalFields;
  }

  /**
   * Returns a key value as string from the given data graph
   * 
   * @param dataGraph
   *          the data graph
   * @return the key value
   */
  public abstract Object getKey(commonj.sdo.DataGraph dataGraph);

  /**
   * Returns a key value as string from the given data object
   * 
   * @param dataObject
   *          the root data object
   * @return the key value
   */
  public abstract Object getKey(DataObject dataObject);

  /**
   * Returns a key value as string from the given type
   * 
   * @param type
   *          the type
   * @return the key value
   */
  public abstract Object getKey(PlasmaType type);

  /**
   * Returns a key value as bytes from the given data graph
   * 
   * @param dataGraph
   *          the data graph
   * @return the key value
   */
  // public abstract byte[] getKeyBytes(commonj.sdo.DataGraph dataGraph);

  /**
   * Returns a key value as bytes from the given data object
   * 
   * @param dataObject
   *          the root data object
   * @return the key value
   */
  // public abstract byte[] getKeyBytes(DataObject dataObject);

  /**
   * Returns the maximum length allowed for this key field.
   * 
   * @return the maximum length allowed for this key field.
   */
  public abstract int getMaxLength();

  public abstract DataType getDataType();

  public abstract DataFlavor getDataFlavor();

  public KeyFieldCodecType getCodecType() {
    return this.field.getCodecType();
  }

  /**
   * Returns the specified key field codec for the field.
   * 
   * @return the specified key field codec for the field.
   */
  public KeyFieldCodec getCodec() {
    if (this.keyFieldWriter == null) {
      switch (this.field.getCodecType()) {
      case PAD:
        this.keyFieldWriter = new PaddingKeyFieldCodec(this);
        break;
      case HASH:
        if (this.dataGraph.getTable().hasHashAlgorithm()) {
          if (this.dataGraph.getTable().getTable().getHashAlgorithm() != null) {
            HashAlgorithm algo = this.dataGraph.getTable().getTable().getHashAlgorithm();
            this.keyFieldWriter = new HashingKeyFieldCodec(this, algo.getName());
          }
        }
        if (this.keyFieldWriter == null)
          this.keyFieldWriter = new HashingKeyFieldCodec(this, HashAlgorithmName.JENKINS);
        break;
      case LEXICOHASH:
        if (this.dataGraph.getTable().hasHashAlgorithm()) {
          if (this.dataGraph.getTable().getTable().getHashAlgorithm() != null) {
            HashAlgorithm algo = this.dataGraph.getTable().getTable().getHashAlgorithm();
            this.keyFieldWriter = new LexicographicHashingKeyFieldCodec(this, algo.getName());
          }
        }
        if (this.keyFieldWriter == null)
          this.keyFieldWriter = new LexicographicHashingKeyFieldCodec(this,
              HashAlgorithmName.JENKINS);
        break;
      case NATIVE:
        this.keyFieldWriter = new NativeKeyFieldCodec(this);
        break;
      case MINBYTE:
        this.keyFieldWriter = new MinBytesKeyFieldCodec(this);
        break;
      default:
        this.keyFieldWriter = new PaddingKeyFieldCodec(this);
        break;
      }
    }
    return this.keyFieldWriter;
  }

}
