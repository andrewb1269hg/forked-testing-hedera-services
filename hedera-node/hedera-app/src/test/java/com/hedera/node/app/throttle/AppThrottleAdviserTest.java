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

package com.hedera.node.app.throttle;

import static com.hedera.hapi.node.base.HederaFunctionality.CONTRACT_CALL;
import static com.hedera.hapi.node.base.HederaFunctionality.CRYPTO_TRANSFER;
import static com.hedera.hapi.node.base.ResponseCodeEnum.REVERTED_SUCCESS;
import static com.hedera.hapi.node.base.ResponseCodeEnum.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.hedera.hapi.node.base.AccountID;
import com.hedera.hapi.node.base.SignatureMap;
import com.hedera.hapi.node.base.Transaction;
import com.hedera.hapi.node.base.TransactionID;
import com.hedera.hapi.node.contract.ContractCallTransactionBody;
import com.hedera.hapi.node.token.CryptoTransferTransactionBody;
import com.hedera.hapi.node.transaction.TransactionBody;
import com.hedera.node.app.workflows.TransactionInfo;
import com.hedera.node.app.workflows.handle.record.RecordListBuilder;
import com.hedera.node.app.workflows.handle.record.SingleTransactionRecordBuilderImpl;
import com.hedera.node.app.workflows.handle.stack.SavepointStackImpl;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppThrottleAdviserTest {

    private static final long GAS_LIMIT = 456L;
    private static final AccountID PAYER_ACCOUNT_ID =
            AccountID.newBuilder().accountNum(1_234).build();
    private static final TransactionBody CONTRACT_CALL_TXN_BODY = TransactionBody.newBuilder()
            .transactionID(
                    TransactionID.newBuilder().accountID(PAYER_ACCOUNT_ID).build())
            .contractCall(
                    ContractCallTransactionBody.newBuilder().gas(GAS_LIMIT).build())
            .build();
    private static final TransactionInfo CONTRACT_CALL_TXN_INFO = new TransactionInfo(
            Transaction.DEFAULT, CONTRACT_CALL_TXN_BODY, SignatureMap.DEFAULT, Bytes.EMPTY, CONTRACT_CALL);
    private static final TransactionBody CRYPTO_TRANSFER_TXN_BODY = TransactionBody.newBuilder()
            .transactionID(
                    TransactionID.newBuilder().accountID(PAYER_ACCOUNT_ID).build())
            .cryptoTransfer(CryptoTransferTransactionBody.DEFAULT)
            .build();
    private static final TransactionInfo CRYPTO_TRANSFER_TXN_INFO = new TransactionInfo(
            Transaction.DEFAULT, CRYPTO_TRANSFER_TXN_BODY, SignatureMap.DEFAULT, Bytes.EMPTY, CRYPTO_TRANSFER);

    @Mock
    private NetworkUtilizationManager networkUtilizationManager;

    @Mock
    private RecordListBuilder recordListBuilder;

    @Mock
    private SavepointStackImpl stack;

    @Mock
    private SingleTransactionRecordBuilderImpl oneChildBuilder;

    @Mock
    private SingleTransactionRecordBuilderImpl twoChildBuilder;

    private static final Instant CONSENSUS_NOW = Instant.parse("2007-12-03T10:15:30.00Z");

    private AppThrottleAdviser subject;

    @BeforeEach
    void setup() {
        subject = new AppThrottleAdviser(networkUtilizationManager, CONSENSUS_NOW, recordListBuilder, stack);
    }

    @Test
    void forwardsShouldThrottleNOfUnscaled() {
        subject.shouldThrottleNOfUnscaled(2, CRYPTO_TRANSFER);
        verify(networkUtilizationManager).shouldThrottleNOfUnscaled(2, CRYPTO_TRANSFER, CONSENSUS_NOW);
    }

    @Test
    void allowsThrottleCapacityForChildrenIfNoneShouldThrottle() {
        given(recordListBuilder.childRecordBuilders()).willReturn(List.of(oneChildBuilder, twoChildBuilder));
        given(oneChildBuilder.status()).willReturn(SUCCESS);
        given(oneChildBuilder.transaction()).willReturn(CRYPTO_TRANSFER_TXN_INFO.transaction());
        given(oneChildBuilder.transactionBody()).willReturn(CRYPTO_TRANSFER_TXN_INFO.txBody());
        given(twoChildBuilder.status()).willReturn(REVERTED_SUCCESS);

        assertThat(subject.hasThrottleCapacityForChildTransactions()).isTrue();
    }

    @Test
    void doesntAllowThrottleCapacityForChildrenIfOneShouldThrottle() {
        given(recordListBuilder.childRecordBuilders()).willReturn(List.of(oneChildBuilder, twoChildBuilder));
        given(oneChildBuilder.status()).willReturn(SUCCESS);
        given(oneChildBuilder.transaction()).willReturn(CONTRACT_CALL_TXN_INFO.transaction());
        given(oneChildBuilder.transactionBody()).willReturn(CONTRACT_CALL_TXN_INFO.txBody());
        given(twoChildBuilder.status()).willReturn(SUCCESS);
        given(twoChildBuilder.transaction()).willReturn(CRYPTO_TRANSFER_TXN_INFO.transaction());
        given(twoChildBuilder.transactionBody()).willReturn(CRYPTO_TRANSFER_TXN_INFO.txBody());
        given(networkUtilizationManager.shouldThrottle(any(), eq(stack), eq(CONSENSUS_NOW)))
                .willReturn(true);
        assertThat(subject.hasThrottleCapacityForChildTransactions()).isFalse();
    }
}