/*
 * Copyright 2020 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.graphscope.common.ir.rel.graph.match;

import com.alibaba.graphscope.common.ir.tools.AliasInference;
import com.google.common.collect.ImmutableList;

import org.apache.calcite.plan.GraphOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelVisitor;
import org.apache.calcite.rel.SingleRel;
import org.apache.calcite.rel.hint.RelHint;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * A wrapper structure for all related graph operators
 */
public abstract class AbstractLogicalMatch extends SingleRel {
    protected AbstractLogicalMatch(
            GraphOptCluster cluster, @Nullable List<RelHint> hints, @Nullable RelNode input) {
        super(cluster, RelTraitSet.createEmpty(), input);
    }

    @Override
    public List<RelNode> getInputs() {
        return this.input == null ? ImmutableList.of() : ImmutableList.of(this.input);
    }

    protected void addFields(List<RelDataTypeField> addTo, RelNode topNode) {
        RelVisitor visitor =
                new RelVisitor() {
                    @Override
                    public void visit(RelNode node, int ordinal, @Nullable RelNode parent) {
                        super.visit(node, ordinal, parent);
                        List<RelDataTypeField> fields = node.getRowType().getFieldList();
                        for (RelDataTypeField field : fields) {
                            if (!field.getName().equals(AliasInference.DEFAULT_NAME)) {
                                addTo.add(field);
                            }
                        }
                    }
                };
        visitor.go(topNode);
    }

    @Override
    public void childrenAccept(RelVisitor visitor) {
        if (this.input != null) {
            visitor.visit(this.input, 0, this);
        }
    }
}
