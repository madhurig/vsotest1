// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.ui.checkout;

import com.microsoft.alm.plugin.idea.ui.common.LoginPageModel;
import com.microsoft.alm.plugin.idea.ui.common.forms.LoginForm;
import com.microsoft.alm.plugin.idea.ui.controls.UserAccountPanel;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

/**
 * This class binds the UI with the Model by attaching listeners to both and keeping them
 * in sync.
 */
class CheckoutPageController implements Observer, ActionListener {
    private final CheckoutPage page;
    private final CheckoutPageModel model;

    public CheckoutPageController(final CheckoutPageModel model, final CheckoutPage page) {
        this.model = model;
        this.model.addObserver(this);
        this.page = page;
        this.page.addActionListener(this);

        // Initialize the form with the current values from the model
        update(null, null);
    }

    public JPanel getPageAsPanel() {
        if (page instanceof JPanel) {
            return (JPanel) page;
        }

        return null;
    }

    public JComponent getComponent(final String name) {
        return page.getComponent(name);
    }

    @Override
    public void update(final Observable o, final Object arg) {
        if (arg == null || arg.equals(LoginPageModel.PROP_CONNECTED)) {
            page.setLoginShowing(!model.isConnected());
        }
        if (arg == null || arg.equals(CheckoutPageModel.PROP_LOADING)) {
            page.setLoading(model.isLoading());
        }
        if (arg == null || arg.equals(LoginPageModel.PROP_AUTHENTICATING)) {
            page.setAuthenticating(model.isAuthenticating());
        }
        if (arg == null || arg.equals(CheckoutPageModel.PROP_DIRECTORY_NAME)) {
            page.setDirectoryName(model.getDirectoryName());
        }
        if (arg == null || arg.equals(CheckoutPageModel.PROP_PARENT_DIR)) {
            page.setParentDirectory(model.getParentDirectory());
        }
        if (arg == null || arg.equals(CheckoutPageModel.PROP_REPO_FILTER)) {
            page.setRepositoryFilter(model.getRepositoryFilter());
        }
        if (arg == null || arg.equals(LoginPageModel.PROP_USER_NAME)) {
            page.setUserName(model.getUserName());
        }
        if (arg == null || arg.equals(LoginPageModel.PROP_SERVER_NAME)) {
            page.setServerName(model.getServerName());
        }
        if (arg == null) {
            page.setRepositoryTable(model.getTableModel(), model.getTableSelectionModel());
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        // Update model from page before we initiate any actions
        updateModel();

        if (LoginForm.CMD_SIGN_IN.equals(e.getActionCommand()) || LoginForm.CMD_ENTER_KEY.equals(e.getActionCommand())) {
            // User pressed Enter or clicked sign in on the login page
            // Asynchronously query for repositories, will prompt for login if needed
            model.clearErrors();
            model.loadRepositories();
        } else if (CheckoutForm.CMD_REFRESH.equals(e.getActionCommand())) {
            // Reload the table (the refresh button shouldn't be visible if the query is currently running)
            model.clearErrors();
            model.loadRepositories();
        } else if (UserAccountPanel.CMD_SIGN_OUT.equals(e.getActionCommand())) {
            // Go back to a disconnected state
            model.clearErrors();
            model.setConnected(false);
            model.signOut();
        } else if (CheckoutForm.CMD_REPO_FILTER_CHANGED.equals(e.getActionCommand())) {
            // No action needed here. We updated the model above which should filter the list automatically.
        } else if (LoginForm.CMD_CREATE_ACCOUNT.equals(e.getActionCommand())) {
            model.gotoLink(CheckoutPageModel.URL_CREATE_ACCOUNT);
        }
    }

    public void updateModel() {
        model.setParentDirectory(page.getParentDirectory());
        model.setDirectoryName(page.getDirectoryName());
        model.setRepositoryFilter(page.getRepositoryFilter());
        model.setServerName(page.getServerName());
    }
}
