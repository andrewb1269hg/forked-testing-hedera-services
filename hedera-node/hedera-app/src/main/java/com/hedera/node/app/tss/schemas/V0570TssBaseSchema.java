/*
 * Copyright (C) 2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hedera.node.app.tss.schemas;

import static com.hedera.hapi.node.state.tss.RosterToKey.ACTIVE_ROSTER;
import static com.hedera.hapi.node.state.tss.TssKeyingStatus.WAITING_FOR_ENCRYPTION_KEYS;

import com.hedera.hapi.node.base.SemanticVersion;
import com.hedera.hapi.node.state.common.EntityNumber;
import com.hedera.hapi.node.state.tss.TssStatus;
import com.hedera.hapi.services.auxiliary.tss.TssEncryptionKeyTransactionBody;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import com.swirlds.state.lifecycle.MigrationContext;
import com.swirlds.state.lifecycle.Schema;
import com.swirlds.state.lifecycle.StateDefinition;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Set;

/**
 * Schema for the TSS service.
 */
public class V0570TssBaseSchema extends Schema {
    public static final String TSS_STATUS_KEY = "TSS_STATUS";
    public static final String TSS_ENCRYPTION_KEY_MAP_KEY = "TSS_ENCRYPTION_KEY";
    /**
     * This will at most be equal to the number of nodes in the network.
     */
    private static final long MAX_TSS_ENCRYPTION_KEYS = 65_536L;

    /**
     * The version of the schema.
     */
    private static final SemanticVersion VERSION =
            SemanticVersion.newBuilder().major(0).minor(57).patch(0).build();

    /**
     * Create a new instance
     */
    public V0570TssBaseSchema() {
        super(VERSION);
    }

    @Override
    public void migrate(@NonNull final MigrationContext ctx) {
        final var tssStatusState = ctx.newStates().getSingleton(TSS_STATUS_KEY);
        if (tssStatusState.get() == null) {
            tssStatusState.put(new TssStatus(WAITING_FOR_ENCRYPTION_KEYS, ACTIVE_ROSTER, Bytes.EMPTY));
        }
    }

    @NonNull
    @Override
    public Set<StateDefinition> statesToCreate() {
        return Set.of(
                StateDefinition.singleton(TSS_STATUS_KEY, TssStatus.PROTOBUF),
                StateDefinition.onDisk(
                        TSS_ENCRYPTION_KEY_MAP_KEY,
                        EntityNumber.PROTOBUF,
                        TssEncryptionKeyTransactionBody.PROTOBUF,
                        MAX_TSS_ENCRYPTION_KEYS));
    }
}
