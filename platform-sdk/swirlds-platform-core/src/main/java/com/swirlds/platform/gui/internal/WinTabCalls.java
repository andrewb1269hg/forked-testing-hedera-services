/*
 * Copyright (C) 2016-2024 Hedera Hashgraph, LLC
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

package com.swirlds.platform.gui.internal;

import com.swirlds.platform.gui.GuiConstants;
import com.swirlds.platform.gui.components.PrePaintableJPanel;
import javax.swing.JLabel;

/**
 * The tab in the Browser window that shows available apps, running swirlds, and saved swirlds.
 */
class WinTabCalls extends PrePaintableJPanel {
    private static final long serialVersionUID = 1L;

    public WinTabCalls() {
        JLabel label = new JLabel("There are no recent calls.");
        label.setFont(GuiConstants.FONT);
        add(label);
    }

    /** {@inheritDoc} */
    public void prePaint() {}
}
