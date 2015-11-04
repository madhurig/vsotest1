// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.ui.common;

import com.intellij.util.ui.JBUI;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import java.awt.AWTKeyStroke;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is a place for static methods that help with Java Swing components.
 */
public class SwingHelper {

    /**
     * This method sets the FocusTraversalKeys for a component to be the standard keys.
     * Use this on Tables or TextAreas where you want the tab keys to leave the control.
     *
     * @param component the component that you want to fix tab keys for
     */
    public static void fixTabKeys(final JComponent component) {
        final Set<AWTKeyStroke> forward = new HashSet<AWTKeyStroke>(
                component.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forward.add(KeyStroke.getKeyStroke("TAB"));
        component.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forward);
        final Set<AWTKeyStroke> backward = new HashSet<AWTKeyStroke>(
                component.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backward.add(KeyStroke.getKeyStroke("shift TAB"));
        component.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backward);
    }

    public static void setPreferredHeight(final JComponent component, final int height) {
        final Dimension size = component.getPreferredSize();
        size.setSize(size.getWidth(), JBUI.scale(height));
        component.setPreferredSize(size);
    }

    public static void copyFontAndMargins(final JTextArea target, final JComponent source) {
        final Insets insets = source.getInsets();
        target.setFont(source.getFont());
        target.setMargin(insets);
    }
}
