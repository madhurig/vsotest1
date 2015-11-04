// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.ui.common;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsNotifier;
import com.intellij.util.PlatformIcons;
import com.microsoft.alm.plugin.idea.resources.Icons;
import com.microsoft.alm.plugin.idea.resources.TfPluginBundle;
import com.microsoft.alm.plugin.idea.ui.common.forms.FeedbackForm;
import com.microsoft.alm.plugin.telemetry.TfsTelemetryConstants;
import com.microsoft.alm.plugin.telemetry.TfsTelemetryHelper;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FeedbackAction extends AbstractAction {
    private final Project project;
    private final String feedbackContextInfo;
    private static final String CMD_SEND_SMILE = "sendSmile";
    private static final String CMD_SEND_FROWN = "sendFrown";
    private static final String URL_PRIVACY_POLICY = "http://go.microsoft.com/fwlink/?LinkID=277167"; // This is the same URL used by Visual Studio Send a Smile

    public FeedbackAction(final Project project, final String feedbackContextInfo) {
        super(TfPluginBundle.message(TfPluginBundle.KEY_FEEDBACK_DIALOG_TITLE), PlatformIcons.COMBOBOX_ARROW_ICON);
        this.project = project;
        this.feedbackContextInfo = feedbackContextInfo;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        assert e != null;

        if (e.getSource() instanceof Component) {
            final Component buttonSource = (Component) e.getSource();
            final JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(createMenuItem(TfPluginBundle.KEY_FEEDBACK_DIALOG_OK_SMILE, Icons.Smile, CMD_SEND_SMILE));
            popupMenu.add(createMenuItem(TfPluginBundle.KEY_FEEDBACK_DIALOG_OK_FROWN, Icons.Frown, CMD_SEND_FROWN));
            popupMenu.show(buttonSource, 0, buttonSource.getHeight());
        }
    }

    private JMenuItem createMenuItem(final String resourceKey, final Icon icon, final String actionCommand) {
        final String text = TfPluginBundle.message(resourceKey);
        final JMenuItem menuItem = new JMenuItem(text, icon);
        menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                menuItemAction(e);
            }
        });

        return menuItem;
    }

    private void menuItemAction(final ActionEvent e) {
        final boolean smile = CMD_SEND_SMILE.equalsIgnoreCase(e.getActionCommand());
        final FeedbackDialog dialog = new FeedbackDialog(project, smile);

        dialog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (FeedbackForm.CMD_GOTO_PRIVACY.equalsIgnoreCase(e.getActionCommand())) {
                    BrowserUtil.browse(URL_PRIVACY_POLICY);
                }
            }
        });

        if (dialog.showAndGet()) {
            // Get comment and email and add telemetry entry
            final String comment = dialog.getComment();
            final String email = dialog.getEmail();
            final String eventName = e.getActionCommand();
            TfsTelemetryHelper.getInstance().sendEvent(eventName,
                    new TfsTelemetryHelper.PropertyMapBuilder()
                            .activeServerContext()
                            .pair(TfsTelemetryConstants.FEEDBACK_PROPERTY_COMMENT, comment)
                            .pair(TfsTelemetryConstants.FEEDBACK_PROPERTY_EMAIL, email)
                            .pair(TfsTelemetryConstants.FEEDBACK_PROPERTY_CONTEXT, feedbackContextInfo)
                            .build());
            VcsNotifier.getInstance(project).notifySuccess(
                    TfPluginBundle.message(TfPluginBundle.KEY_FEEDBACK_DIALOG_TITLE),
                    TfPluginBundle.message(TfPluginBundle.KEY_FEEDBACK_NOTIFICATION));
        }
    }
}
