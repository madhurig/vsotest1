// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.ui.checkout;

import com.microsoft.alm.plugin.idea.ui.common.ServerContextTableModel;

import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import java.awt.event.ActionListener;

/**
 * This interface exists to make testing the controller possible
 */
public interface CheckoutPage {

    void addActionListener(ActionListener listener);

    void setLoginShowing(boolean showLogin);

    void setLoading(boolean loading);

    void setAuthenticating(boolean authenticating);

    void setRepositoryFilter(String filter);

    String getRepositoryFilter();

    void setRepositoryTable(ServerContextTableModel tableModel, ListSelectionModel selectionModel);

    void setParentDirectory(String path);

    String getParentDirectory();

    void setDirectoryName(String name);

    String getDirectoryName();

    void setUserName(String name);

    void setServerName(String name);

    String getServerName();

    JComponent getComponent(String name);
}
