// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.ui.checkout;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.alm.plugin.authentication.AuthenticationInfo;
import com.microsoft.alm.plugin.context.ServerContext;
import com.microsoft.alm.plugin.idea.resources.TfPluginBundle;
import com.microsoft.alm.plugin.idea.ui.common.LoginPageModelImpl;
import com.microsoft.alm.plugin.idea.ui.common.ModelValidationInfo;
import com.microsoft.alm.plugin.idea.ui.common.ServerContextLookupListener;
import com.microsoft.alm.plugin.idea.ui.common.ServerContextLookupPageModel;
import com.microsoft.alm.plugin.idea.ui.common.ServerContextTableModel;
import com.microsoft.alm.plugin.telemetry.TfsTelemetryHelper;
import git4idea.commands.Git;
import org.apache.commons.lang.StringUtils;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * This class is provided as a base for the VSO and TFS models. It provides the majority of the
 * functionality with a few abstract methods that must be overridden.
 */
public abstract class CheckoutPageModelImpl extends LoginPageModelImpl implements CheckoutPageModel, ServerContextLookupPageModel {
    private CheckoutModel parentModel;
    private boolean loading = false;
    private boolean cloneEnabled = false;
    //default values for Strings should be "" rather than null.
    private String parentDirectory = "";
    private String directoryName = "";
    private String repositoryFilter = "";
    private final ServerContextTableModel repositoryTableModel;
    private final ServerContextLookupListener repositoryProvider;

    public CheckoutPageModelImpl(final CheckoutModel checkoutModel, final ServerContextTableModel.Column[] columns) {
        super(checkoutModel);
        parentModel = checkoutModel;

        // Create table model (subclasses should modify the table model as needed)
        repositoryTableModel = new ServerContextTableModel(columns);

        // Attach listeners
        setupSelectionListener();

        // Default the parent directory
        parentDirectory = DEFAULT_SOURCE_PATH;

        // Create the default repository provider
        repositoryProvider = new ServerContextLookupListener(this);
    }

    /**
     * This getter allows the derived classes to access the CheckoutModel that owns them.
     */
    protected CheckoutModel getParentModel() {
        return parentModel;
    }

    /**
     * This setter allows the tests to set the parent model.
     */
    protected void setParentModel(CheckoutModel parentModel) {
        this.parentModel = parentModel;
    }

    protected ServerContextLookupListener getRepositoryProvider() {
        return this.repositoryProvider;
    }

    /**
     * Subclasses must override this to return the authentication info object.
     * Return null if authentication is not complete.
     */
    protected abstract AuthenticationInfo getAuthenticationInfo();

    /**
     * Overriding SignOut to do a couple additional things.
     */
    @Override
    public void signOut() {
        super.signOut();
        setConnectionStatus(false);
        setLoading(false);
        clearContexts();
    }

    @Override
    public String getParentDirectory() {
        return parentDirectory;
    }

    @Override
    public void setParentDirectory(final String parentDirectory) {
        if (!StringUtils.equals(this.parentDirectory, parentDirectory)) {
            this.parentDirectory = parentDirectory;
            setChangedAndNotify(PROP_PARENT_DIR);
        }
    }

    @Override
    public String getDirectoryName() {
        return directoryName;
    }

    @Override
    public void setDirectoryName(final String directoryName) {
        if (!StringUtils.equals(this.directoryName, directoryName)) {
            this.directoryName = directoryName;
            setChangedAndNotify(PROP_DIRECTORY_NAME);
        }
    }

    @Override
    public String getRepositoryFilter() {
        return repositoryFilter;
    }

    @Override
    public void setRepositoryFilter(final String repositoryFilter) {
        if (!StringUtils.equals(this.repositoryFilter, repositoryFilter)) {
            this.repositoryFilter = repositoryFilter;
            setChangedAndNotify(PROP_REPO_FILTER);
            repositoryTableModel.setFilter(repositoryFilter);
        }
    }

    @Override
    public boolean isLoading() {
        return loading;
    }

    @Override
    public void setLoading(final boolean loading) {
        if (this.loading != loading) {
            this.loading = loading;
            setChangedAndNotify(PROP_LOADING);
        }
    }

    @Override
    public void setCloneEnabled(final boolean cloneEnabled) {
        if (this.cloneEnabled != cloneEnabled) {
            this.cloneEnabled = cloneEnabled;
            getParentModel().updateCloneEnabled();
        }
    }

    protected void setConnectionStatus(boolean connected) {
        setConnected(connected);
        setCloneEnabled(connected);
    }

    @Override
    public ServerContextTableModel getTableModel() {
        return repositoryTableModel;
    }

    @Override
    public ListSelectionModel getTableSelectionModel() {
        return repositoryTableModel.getSelectionModel();
    }

    @Override
    public ModelValidationInfo validate() {
        ModelValidationInfo result = super.validate();

        if (result == ModelValidationInfo.NO_ERRORS) {
            final String parentDirectory = getParentDirectory();
            if (parentDirectory == null || parentDirectory.isEmpty()) {
                return ModelValidationInfo.createWithResource(PROP_PARENT_DIR,
                        TfPluginBundle.KEY_CHECKOUT_DIALOG_ERRORS_PARENT_DIR_EMPTY);
            }

            final File parentDirectoryOnDisk = new File(parentDirectory);
            if (!parentDirectoryOnDisk.exists()) {
                return ModelValidationInfo.createWithResource(PROP_PARENT_DIR,
                        TfPluginBundle.KEY_CHECKOUT_DIALOG_ERRORS_PARENT_DIR_NOT_FOUND);
            }

            // We test this method and so we need to check to see if we are in IntelliJ before using VirtualFileManager
            // ApplicationManager is null if we are not in IntelliJ
            if (ApplicationManager.getApplication() != null) {
                final VirtualFile destinationParent = LocalFileSystem.getInstance().findFileByPath(parentDirectory);
                if (destinationParent == null) {
                    return ModelValidationInfo.createWithResource(PROP_PARENT_DIR,
                            TfPluginBundle.KEY_CHECKOUT_DIALOG_ERRORS_PARENT_DIR_NOT_FOUND);
                }
            }

            if (getSelectedContext() == null) {
                return ModelValidationInfo.createWithResource(PROP_REPO_TABLE,
                        TfPluginBundle.KEY_CHECKOUT_DIALOG_ERRORS_REPO_NOT_SELECTED);
            }

            final String directoryName = getDirectoryName();
            if (directoryName == null || directoryName.isEmpty()) {
                return ModelValidationInfo.createWithResource(PROP_DIRECTORY_NAME,
                        TfPluginBundle.KEY_CHECKOUT_DIALOG_ERRORS_DIR_NAME_EMPTY);
            }

            final File destDirectoryOnDisk = new File(parentDirectory, directoryName);
            //verify the destination directory does not exist
            if (destDirectoryOnDisk.exists() && destDirectoryOnDisk.isDirectory()) {
                return ModelValidationInfo.createWithResource(PROP_DIRECTORY_NAME,
                        TfPluginBundle.KEY_CHECKOUT_DIALOG_ERRORS_DESTINATION_EXISTS, directoryName);
            }
            //verify destination directory parent exists, we can reach this condition if user specifies a path for directory name
            if (destDirectoryOnDisk.getParentFile() == null || !destDirectoryOnDisk.getParentFile().exists()) {
                return ModelValidationInfo.createWithResource(PROP_DIRECTORY_NAME,
                        TfPluginBundle.KEY_CHECKOUT_DIALOG_ERRORS_DIR_NAME_INVALID,
                        directoryName, destDirectoryOnDisk.getParent());
            }

        } else {
            return result;
        }

        return ModelValidationInfo.NO_ERRORS;
    }

    @Override
    public abstract void loadRepositories();

    @Override
    public void cloneSelectedRepo() {
        final ModelValidationInfo validationInfo = validate();
        if (validationInfo == null) {
            final ServerContext context = getSelectedContext();
            // The url string obtained from the REST SDK is not encoded.
            // Replace space which is the only known valid character in team project and repository name that is not a valid character in URI
            final String gitRepositoryStr = context.getGitRepository().getRemoteUrl().replace(" ", "%20");

            // The base LoginPageModel manages the context for us
            super.completeSignIn(context);

            final VirtualFile destinationParent = LocalFileSystem.getInstance().findFileByIoFile(
                    new File(getParentDirectory()));

            final Git git = ServiceManager.getService(Git.class);
            git4idea.checkout.GitCheckoutProvider.clone(getParentModel().getProject(), git, getParentModel().getListener(),
                    destinationParent,
                    gitRepositoryStr,
                    getDirectoryName(),
                    getParentDirectory());

            // TODO: need a way to tell if/when the clone actually succeeded or failed
            // Add Telemetry for a successful clone
            final String action = "clone";
            TfsTelemetryHelper.getInstance().sendEvent(action, new TfsTelemetryHelper.PropertyMapBuilder()
                    .currentOrActiveContext(context)
                    .actionName(action)
                    .success(true).build());
        }
    }

    /**
     * This method is provided to allow the derived classes an easy way to get the selected repository instance.
     */
    protected ServerContext getSelectedContext() {
        return repositoryTableModel.getSelectedContext();
    }

    /**
     * This method is provided to allow the listener to update the list of contexts.
     */
    @Override
    public void clearContexts() {
        repositoryTableModel.clearRows();
    }

    /**
     * This method is provided to allow the listener to update the list of contexts.
     */
    public void appendContexts(final List<ServerContext> serverContexts) {
        repositoryTableModel.addServerContexts(serverContexts);
    }

    /**
     * This method is provided to allow the derived classes an easy way to add to the list of repositories.
     */
    protected void addContext(ServerContext serverContext) {
        appendContexts(Collections.singletonList(serverContext));
    }

    private void setupSelectionListener() {
        // Set up event listener to set the Directory name when the selection changes
        repositoryTableModel.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // No need to change things while the list is adjusting
                if (!e.getValueIsAdjusting()) {
                    final ServerContext row = repositoryTableModel.getSelectedContext();
                    // Get the repository name and set the directory name to match
                    final String repositoryName = (row != null && row.getGitRepository() != null) ? row.getGitRepository().getName() : "";
                    setDirectoryName(repositoryName);
                }
            }
        });
    }

    @Override
    public void dispose() {
        repositoryProvider.terminateActiveOperation();
    }
}