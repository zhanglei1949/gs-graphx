/**
 * Copyright 2020 Alibaba Group Holding Limited.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.graphscope.groot.operation.dml;

import com.alibaba.graphscope.groot.common.schema.wrapper.EdgeKind;
import com.alibaba.graphscope.groot.operation.EdgeId;
import com.alibaba.graphscope.groot.operation.Operation;
import com.alibaba.graphscope.groot.operation.OperationType;
import com.alibaba.graphscope.proto.groot.DataOperationPb;
import com.alibaba.graphscope.proto.groot.EdgeLocationPb;
import com.google.protobuf.ByteString;

import java.util.List;

public class ClearEdgePropertyOperation extends Operation {

    private EdgeId edgeId;
    private EdgeKind edgeKind;
    private List<Integer> properties;
    private boolean forward;

    public ClearEdgePropertyOperation(
            EdgeId edgeId, EdgeKind edgeKind, List<Integer> properties, boolean forward) {
        super(OperationType.CLEAR_EDGE_PROPERTIES);
        this.edgeId = edgeId;
        this.edgeKind = edgeKind;
        this.properties = properties;
        this.forward = forward;
    }

    @Override
    protected long getPartitionKey() {
        if (forward) {
            return edgeId.getSrcId().getId();
        } else {
            return edgeId.getDstId().getId();
        }
    }

    @Override
    protected ByteString getBytes() {
        DataOperationPb.Builder builder = DataOperationPb.newBuilder();
        builder.setKeyBlob(edgeId.toProto().toByteString());
        builder.setLocationBlob(
                EdgeLocationPb.newBuilder()
                        .setEdgeKind(edgeKind.toOperationProto())
                        .setForward(this.forward)
                        .build()
                        .toByteString());
        builder.addAllPropIds(properties);
        return builder.build().toByteString();
    }
}
