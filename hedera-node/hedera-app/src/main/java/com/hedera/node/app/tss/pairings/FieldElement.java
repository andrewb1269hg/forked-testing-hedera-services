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

package com.hedera.node.app.tss.pairings;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.math.BigInteger;

public interface FieldElement {
    /**
     * Returns the field element as a BigInteger
     *
     * @return the field element as a BigInteger
     */
    @NonNull
    BigInteger toBigInteger();
}