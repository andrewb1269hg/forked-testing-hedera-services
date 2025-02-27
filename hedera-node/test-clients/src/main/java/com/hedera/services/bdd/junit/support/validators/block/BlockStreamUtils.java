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

package com.hedera.services.bdd.junit.support.validators.block;

import com.hedera.hapi.block.stream.output.StateIdentifier;

public class BlockStreamUtils {
    public static String stateNameOf(final int stateId) {
        return switch (StateIdentifier.fromProtobufOrdinal(stateId)) {
            case STATE_ID_NODES -> "AddressBookService.NODES";
            case STATE_ID_BLOCK_INFO -> "BlockRecordService.BLOCKS";
            case STATE_ID_RUNNING_HASHES -> "BlockRecordService.RUNNING_HASHES";
            case STATE_ID_BLOCK_STREAM_INFO -> "BlockStreamService.BLOCK_STREAM_INFO";
            case STATE_ID_CONGESTION_STARTS -> "CongestionThrottleService.CONGESTION_LEVEL_STARTS";
            case STATE_ID_THROTTLE_USAGE -> "CongestionThrottleService.THROTTLE_USAGE_SNAPSHOTS";
            case STATE_ID_TOPICS -> "ConsensusService.TOPICS";
            case STATE_ID_CONTRACT_BYTECODE -> "ContractService.BYTECODE";
            case STATE_ID_CONTRACT_STORAGE -> "ContractService.STORAGE";
            case STATE_ID_ENTITY_ID -> "EntityIdService.ENTITY_ID";
            case STATE_ID_MIDNIGHT_RATES -> "FeeService.MIDNIGHT_RATES";
            case STATE_ID_FILES -> "FileService.FILES";
            case STATE_ID_UPGRADE_DATA_150 -> "FileService.UPGRADE_DATA[FileID[shardNum=0, realmNum=0, fileNum=150]]";
            case STATE_ID_UPGRADE_DATA_151 -> "FileService.UPGRADE_DATA[FileID[shardNum=0, realmNum=0, fileNum=151]]";
            case STATE_ID_UPGRADE_DATA_152 -> "FileService.UPGRADE_DATA[FileID[shardNum=0, realmNum=0, fileNum=152]]";
            case STATE_ID_UPGRADE_DATA_153 -> "FileService.UPGRADE_DATA[FileID[shardNum=0, realmNum=0, fileNum=153]]";
            case STATE_ID_UPGRADE_DATA_154 -> "FileService.UPGRADE_DATA[FileID[shardNum=0, realmNum=0, fileNum=154]]";
            case STATE_ID_UPGRADE_DATA_155 -> "FileService.UPGRADE_DATA[FileID[shardNum=0, realmNum=0, fileNum=155]]";
            case STATE_ID_UPGRADE_DATA_156 -> "FileService.UPGRADE_DATA[FileID[shardNum=0, realmNum=0, fileNum=156]]";
            case STATE_ID_UPGRADE_DATA_157 -> "FileService.UPGRADE_DATA[FileID[shardNum=0, realmNum=0, fileNum=157]]";
            case STATE_ID_UPGRADE_DATA_158 -> "FileService.UPGRADE_DATA[FileID[shardNum=0, realmNum=0, fileNum=158]]";
            case STATE_ID_UPGRADE_DATA_159 -> "FileService.UPGRADE_DATA[FileID[shardNum=0, realmNum=0, fileNum=159]]";
            case STATE_ID_UPGRADE_FILE -> "FileService.UPGRADE_FILE";
            case STATE_ID_FREEZE_TIME -> "FreezeService.FREEZE_TIME";
            case STATE_ID_UPGRADE_FILE_HASH -> "FreezeService.UPGRADE_FILE_HASH";
            case STATE_ID_PLATFORM_STATE -> "PlatformStateService.PLATFORM_STATE";
            case STATE_ID_ROSTER_STATE -> "RosterService.ROSTER_STATE";
            case STATE_ID_ROSTERS -> "RosterService.ROSTERS";
            case STATE_ID_TRANSACTION_RECEIPTS_QUEUE -> "RecordCache.TransactionReceiptQueue";
            case STATE_ID_SCHEDULES_BY_EQUALITY -> "ScheduleService.SCHEDULES_BY_EQUALITY";
            case STATE_ID_SCHEDULES_BY_EXPIRY -> "ScheduleService.SCHEDULES_BY_EXPIRY_SEC";
            case STATE_ID_SCHEDULES_BY_ID -> "ScheduleService.SCHEDULES_BY_ID";
            case STATE_ID_SCHEDULE_ID_BY_EQUALITY -> "ScheduleService.SCHEDULE_ID_BY_EQUALITY";
            case STATE_ID_SCHEDULED_COUNTS -> "ScheduleService.SCHEDULED_COUNTS";
            case STATE_ID_SCHEDULED_ORDERS -> "ScheduleService.SCHEDULED_ORDERS";
            case STATE_ID_SCHEDULED_USAGES -> "ScheduleService.SCHEDULED_USAGES";
            case STATE_ID_ACCOUNTS -> "TokenService.ACCOUNTS";
            case STATE_ID_ALIASES -> "TokenService.ALIASES";
            case STATE_ID_NFTS -> "TokenService.NFTS";
            case STATE_ID_PENDING_AIRDROPS -> "TokenService.PENDING_AIRDROPS";
            case STATE_ID_STAKING_INFO -> "TokenService.STAKING_INFOS";
            case STATE_ID_NETWORK_REWARDS -> "TokenService.STAKING_NETWORK_REWARDS";
            case STATE_ID_TOKEN_RELATIONS -> "TokenService.TOKEN_RELS";
            case STATE_ID_TOKENS -> "TokenService.TOKENS";
            case STATE_ID_TSS_MESSAGES -> "TssBaseService.TSS_MESSAGES";
            case STATE_ID_TSS_VOTES -> "TssBaseService.TSS_VOTES";
            case STATE_ID_TSS_ENCRYPTION_KEYS -> "TssBaseService.TSS_ENCRYPTION_KEY";
            case STATE_ID_TSS_STATUS -> "TssBaseService.TSS_STATUS";
        };
    }
}
