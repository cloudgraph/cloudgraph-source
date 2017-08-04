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
package org.cloudgraph.hbase.mutation;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.common.CloudGraphConstants;
import org.cloudgraph.hbase.io.DistributedWriter;
import org.cloudgraph.hbase.io.EdgeWriter;
import org.cloudgraph.hbase.io.RowWriter;
import org.cloudgraph.hbase.io.TableWriter;
import org.cloudgraph.hbase.service.ServiceContext;
import org.plasma.sdo.PlasmaDataObject;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.access.RequiredPropertyException;
import org.plasma.sdo.core.CoreConstants;
import org.plasma.sdo.core.CoreNode;
import org.plasma.sdo.core.SnapshotMap;
import org.plasma.sdo.profile.ConcurrencyType;
import org.plasma.sdo.profile.ConcurrentDataFlavor;

import commonj.sdo.DataGraph;
import commonj.sdo.Property;

public class Delete extends DefaultMutation implements Collector {
  private static Log log = LogFactory.getLog(Delete.class);

  public Delete(ServiceContext context, SnapshotMap snapshotMap, String username) {
    super(context, snapshotMap, username);
  }

  @Override
  public void collect(DataGraph dataGraph, PlasmaDataObject dataObject,
      DistributedWriter graphWriter, TableWriter context, RowWriter rowWriter) throws IOException,
      IllegalAccessException {
    PlasmaType type = (PlasmaType) dataObject.getType();

    CoreNode coreNode = ((CoreNode) dataObject);
    // FIXME: get rid of cast - define instance properties in 'base type'
    Timestamp snapshotDate = (Timestamp) coreNode
        .getValue(CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP);
    if (snapshotDate == null)
      throw new RequiredPropertyException("instance property '"
          + CoreConstants.PROPERTY_NAME_SNAPSHOT_TIMESTAMP
          + "' is required to update data object, " + dataObject);
    if (log.isDebugEnabled())
      log.debug(dataObject + " timestamp: " + String.valueOf(snapshotDate));

    Long sequence = (Long) coreNode.getValue(CloudGraphConstants.SEQUENCE);
    if (sequence == null)
      throw new RequiredPropertyException("instance property '" + CloudGraphConstants.SEQUENCE
          + "' is required to delete data object, " + dataObject);
    if (log.isDebugEnabled())
      log.debug(dataObject + " (seq: " + sequence + ")");

    this.checkLock(dataObject, type, snapshotDate);
    this.checkOptimistic(dataObject, type, snapshotDate);

    PlasmaProperty concurrencyUserProperty = (PlasmaProperty) type.findProperty(
        ConcurrencyType.optimistic, ConcurrentDataFlavor.user);
    if (concurrencyUserProperty == null) {
      if (log.isDebugEnabled())
        log.debug("could not find optimistic concurrency (username) property for type, "
            + type.getURI() + "#" + type.getName());
    }

    PlasmaProperty concurrencyTimestampProperty = (PlasmaProperty) type.findProperty(
        ConcurrencyType.optimistic, ConcurrentDataFlavor.time);
    if (concurrencyTimestampProperty == null) {
      if (log.isDebugEnabled())
        log.debug("could not find optimistic concurrency timestamp property for type, "
            + type.getURI() + "#" + type.getName());
    }

    // If no tombstones, and if the root of the row is deleted if this
    // object is the root, delete it, else if
    // the given data object is part of the row, just return.
    // No need to delete its individual properties, as the row is going away
    // anyway.
    if (!context.getTableConfig().tombstoneRows()) {
      if (rowWriter.isRootDeleted()) {
        if (rowWriter.getRootDataObject().equals(dataObject)) {
          rowWriter.deleteRow();
          if (log.isDebugEnabled())
            log.debug("deleting root, " + dataObject);
          return;
        } else if (rowWriter.contains(dataObject)) {
          return;
        }
      }
    }

    // For tombstone rows, we want to delete app columns (and leave a
    // tombstone column
    // which is not done here).
    // So delete all columns associated with the entity
    rowWriter.deleteRowEntityMetaData(dataObject, sequence);

    List<Property> properties = type.getProperties();
    for (Property p : properties) {
      PlasmaProperty property = (PlasmaProperty) p;
      if (property.getType().isDataType()) {
        rowWriter.deleteRowData(dataObject, sequence, property);
        // this.deleteDataCell(rowWriter, dataObject, sequence,
        // property);
      } else {
        EdgeWriter edgeWriter = rowWriter.getEdgeWriter(dataObject, property, sequence);
        edgeWriter.delete();
      }
    }
  }

}
