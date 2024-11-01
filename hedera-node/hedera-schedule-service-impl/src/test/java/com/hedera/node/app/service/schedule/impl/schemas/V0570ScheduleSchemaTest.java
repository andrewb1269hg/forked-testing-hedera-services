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

package com.hedera.node.app.service.schedule.impl.schemas;

import static com.hedera.node.app.service.schedule.impl.schemas.V0490ScheduleSchema.SCHEDULES_BY_EQUALITY_KEY;
import static com.hedera.node.app.service.schedule.impl.schemas.V0490ScheduleSchema.SCHEDULES_BY_EXPIRY_SEC_KEY;
import static com.hedera.node.app.service.schedule.impl.schemas.V0570ScheduleSchema.SCHEDULE_IDS_BY_EXPIRY_SEC_KEY;
import static com.hedera.node.app.service.schedule.impl.schemas.V0570ScheduleSchema.SCHEDULE_ID_BY_EQUALITY_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

import com.hedera.hapi.node.base.ScheduleID;
import com.hedera.hapi.node.base.SemanticVersion;
import com.hedera.hapi.node.state.primitives.ProtoBytes;
import com.hedera.hapi.node.state.primitives.ProtoLong;
import com.hedera.hapi.node.state.schedule.ScheduleIdList;
import com.hedera.hapi.node.state.schedule.ScheduleList;
import com.hedera.node.app.service.schedule.impl.ScheduleStoreUtility;
import com.hedera.node.app.service.schedule.impl.ScheduleTestBase;
import com.hedera.node.app.spi.fixtures.state.MapWritableStates;
import com.hedera.node.app.spi.fixtures.util.LogCaptor;
import com.hedera.node.app.spi.fixtures.util.LogCaptureExtension;
import com.hedera.node.app.spi.fixtures.util.LoggingSubject;
import com.hedera.node.app.spi.fixtures.util.LoggingTarget;
import com.hedera.node.app.spi.workflows.PreCheckException;
import com.swirlds.state.spi.MigrationContext;
import com.swirlds.state.spi.StateDefinition;
import com.swirlds.state.test.fixtures.MapReadableKVState;
import com.swirlds.state.test.fixtures.MapReadableStates;
import com.swirlds.state.test.fixtures.MapWritableKVState;
import java.security.InvalidKeyException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class, LogCaptureExtension.class})
class V0570ScheduleSchemaTest extends ScheduleTestBase {
    @LoggingTarget
    private LogCaptor logCaptor;

    @LoggingSubject
    private V0570ScheduleSchema subject;

    @Mock
    private MigrationContext migrationContext;

    private final Map<ProtoLong, ScheduleList> schedulesByExpirySec = new HashMap<>();
    private final Map<ProtoLong, ScheduleIdList> scheduleIdsByExpirySec = new HashMap<>();
    private MapReadableKVState<ProtoLong, ScheduleList> readableSchedulesByExpirySec;
    private MapWritableKVState<ProtoLong, ScheduleIdList> writableScheduleIdsByExpirySec;

    private final Map<ProtoBytes, ScheduleList> schedulesByEquality = new HashMap<>();
    private final Map<ProtoBytes, ScheduleID> scheduleByEquality = new HashMap<>();
    private MapReadableKVState<ProtoBytes, ScheduleList> readableSchedulesByEquality;
    private MapWritableKVState<ProtoBytes, ScheduleID> writableScheduleIdByEquality;

    private MapWritableStates writableStates = null;
    private MapReadableStates readableStates = null;

    @BeforeEach
    void setUp() throws PreCheckException, InvalidKeyException {
        setUpBase();
        subject = new V0570ScheduleSchema();
    }

    @Test
    void constructorHappyPath() {
        Assertions.assertThat(subject.getVersion())
                .isEqualTo(
                        SemanticVersion.newBuilder().major(0).minor(57).patch(0).build());
    }

    @Test
    void statesToCreateIsCorrect() {
        var sortedResult = subject.statesToCreate().stream()
                .sorted(Comparator.comparing(StateDefinition::stateKey))
                .toList();

        final var stateDef1 = sortedResult.getFirst();
        Assertions.assertThat(stateDef1.stateKey()).isEqualTo(SCHEDULE_IDS_BY_EXPIRY_SEC_KEY);
        Assertions.assertThat(stateDef1.keyCodec()).isEqualTo(ProtoLong.PROTOBUF);
        Assertions.assertThat(stateDef1.valueCodec()).isEqualTo(ScheduleIdList.PROTOBUF);

        final var stateDef2 = sortedResult.get(1);
        Assertions.assertThat(stateDef2.stateKey()).isEqualTo(SCHEDULE_ID_BY_EQUALITY_KEY);
        Assertions.assertThat(stateDef2.keyCodec()).isEqualTo(ProtoBytes.PROTOBUF);
        Assertions.assertThat(stateDef2.valueCodec()).isEqualTo(ScheduleID.PROTOBUF);
    }

    @Test
    void testStatesToRemove() {
        Set<String> statesToRemove = subject.statesToRemove();
        assertNotNull(statesToRemove);
        assertEquals(0, statesToRemove.size());
        //        assertTrue(statesToRemove.containsAll(Set.of(SCHEDULES_BY_EXPIRY_SEC_KEY,
        // SCHEDULES_BY_EQUALITY_KEY)));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void restartNullArgThrows() {
        Assertions.assertThatThrownBy(() -> subject.restart(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void restartHappyPath() {
        Assertions.assertThatNoException().isThrownBy(() -> subject.restart(migrationContext));
    }

    @Test
    void migrateAsExpected() {
        setupMigrationContext();

        assertThatCode(() -> subject.migrate(migrationContext)).doesNotThrowAnyException();
        assertThat(logCaptor.infoLogs()).contains("Started migrating Schedule Schema from 0.49.0 to 0.57.0");
        assertThat(logCaptor.infoLogs()).contains("Migrated 1 Schedules from SCHEDULES_BY_EXPIRY_SEC_KEY");
        assertThat(logCaptor.infoLogs()).contains("Migrated 2 Schedules from SCHEDULES_BY_EQUALITY_KEY");
        assertThat(writableScheduleIdsByExpirySec.size()).isEqualTo(1L);
        assertThat(writableScheduleIdByEquality.size()).isEqualTo(2L);
    }

    private void setupMigrationContext() {
        final var scheduler1 =
                otherScheduleInState.copyBuilder().memo("otherMemo").build();
        schedulesByExpirySec.put(
                new ProtoLong(scheduler1.calculatedExpirationSecond()),
                ScheduleList.newBuilder()
                        .schedules(List.of(scheduler1, otherScheduleInState))
                        .build());
        readableSchedulesByExpirySec = new MapReadableKVState<>(SCHEDULES_BY_EXPIRY_SEC_KEY, schedulesByExpirySec);
        writableScheduleIdsByExpirySec =
                new MapWritableKVState<>(SCHEDULE_IDS_BY_EXPIRY_SEC_KEY, scheduleIdsByExpirySec);

        final ProtoBytes protoHash1 = new ProtoBytes(ScheduleStoreUtility.calculateBytesHash(scheduler1));
        final ProtoBytes protoHash2 = new ProtoBytes(ScheduleStoreUtility.calculateBytesHash(otherScheduleInState));
        schedulesByEquality.put(
                protoHash1,
                ScheduleList.newBuilder().schedules(List.of(scheduler1)).build());
        schedulesByEquality.put(
                protoHash2,
                ScheduleList.newBuilder()
                        .schedules(List.of(otherScheduleInState))
                        .build());
        readableSchedulesByEquality = new MapReadableKVState<>(SCHEDULES_BY_EQUALITY_KEY, schedulesByEquality);
        writableScheduleIdByEquality = new MapWritableKVState<>(SCHEDULE_ID_BY_EQUALITY_KEY, scheduleByEquality);

        writableStates = MapWritableStates.builder()
                .state(writableScheduleIdByEquality)
                .state(writableScheduleIdsByExpirySec)
                .build();
        readableStates = MapReadableStates.builder()
                .state(readableSchedulesByExpirySec)
                .state(readableSchedulesByEquality)
                .build();
        given(migrationContext.newStates()).willReturn(writableStates);
        given(migrationContext.previousStates()).willReturn(readableStates);
    }
}