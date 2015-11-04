// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.ui.controls;

import com.intellij.ide.ui.laf.darcula.DarculaUIUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Use this class to draw a clickable link on to a dialog or panel.
 * This is derived from IntelliJ's LinkLabel class and so responds to
 * themes appropriately.
 * It adds the normal ActionListener pattern to the super class.
 */
public class Hyperlink extends LinkLabel<Object> {
    private List<ActionListener> listeners;
    private String actionCommand;

    public Hyperlink() {
        super("Hyperlink", null);

        super.setListener(new LinkListener<Object>() {
            @Override
            public void linkSelected(final LinkLabel aSource, final Object aLinkData) {
                notifyActionListeners();
            }
        }, null);

        // Make our link focusable via the keyboard (Tab key)
        super.setFocusable(true);
    }

    public String getActionCommand() {
        return actionCommand;
    }

    public void setActionCommand(final String actionCommand) {
        this.actionCommand = actionCommand;
    }

    /**
     * We are overriding this method to handle the space bar and enter key events.
     * These key events will trigger the actionPerformed method on all listeners.
     */
    @Override
    protected void processComponentKeyEvent(final KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
            e.consume();
            notifyActionListeners();
        }
    }

    /**
     * We are overriding this method to force a repaint of the control when the focus changes.
     */
    @Override
    protected void processFocusEvent(final FocusEvent e) {
        super.processFocusEvent(e);
        super.repaint();
    }

    /**
     * We are overriding this method to paint a focus rectangle around the control.
     */
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (hasFocus()) {
            g.setColor(JBColor.black);
            DarculaUIUtil.paintFocusRing(g, 0, 0, getWidth(), getHeight());
        }
    }

    private void notifyActionListeners() {
        if (listeners == null || listeners.size() == 0) {
            return;
        }

        final ActionEvent event = new ActionEvent(this, 1, actionCommand);
        for (final ActionListener al : listeners) {
            al.actionPerformed(event);
        }
    }

    public void addActionListener(final ActionListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ActionListener>();
        }

        listeners.add(listener);
    }

    public void removeActionListener(final ActionListener listener) {
        if (listeners == null || listeners.size() == 0) {
            return;
        }

        listeners.remove(listener);
    }
}
