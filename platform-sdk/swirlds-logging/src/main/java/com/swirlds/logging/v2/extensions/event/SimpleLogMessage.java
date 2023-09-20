/*
 * Copyright (C) 2023 Hedera Hashgraph, LLC
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

package com.swirlds.logging.v2.extensions.event;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A simple log message that is just a String that does not need to be handled / parsed / ... in any specific way.
 *
 * @param message The message
 * @see LogMessage
 */
public record SimpleLogMessage(@NonNull String message) implements LogMessage {

    @Override
    public String getMessage() {
        return message;
    }
}