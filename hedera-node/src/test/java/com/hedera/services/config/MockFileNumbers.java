package com.hedera.services.config;

/*-
 * ‌
 * Hedera Services Node
 * ​
 * Copyright (C) 2018 - 2020 Hedera Hashgraph, LLC
 * ​
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
 * ‍
 */

import com.hedera.services.context.properties.PropertySource;
import com.hederahashgraph.api.proto.java.FileID;

public class MockFileNumbers extends FileNumbers {
	public MockFileNumbers() {
		super(null, null);
	}

	@Override
	public long addressBook() {
		return 101;
	}

	@Override
	public long nodeDetails() {
		return 102;
	}

	@Override
	public long feeSchedules() {
		return 111;
	}

	@Override
	public long exchangeRates() {
		return 112;
	}

	@Override
	public long applicationProperties() {
		return 121;
	}

	@Override
	public long apiPermissions() {
		return 122;
	}

	@Override
	public FileID toFid(long num) {
		return FileID.newBuilder()
				.setRealmNum(0)
				.setShardNum(0)
				.setFileNum(num)
				.build();
	}
}
