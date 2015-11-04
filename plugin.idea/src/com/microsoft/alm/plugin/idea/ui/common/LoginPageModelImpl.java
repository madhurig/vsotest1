// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.ui.common;

import com.intellij.ide.BrowserUtil;
import com.microsoft.alm.common.utils.UrlHelper;
import com.microsoft.alm.plugin.authentication.VsoAuthenticationProvider;
import com.microsoft.alm.plugin.context.ServerContext;
import com.microsoft.alm.plugin.context.ServerContextManager;
import com.microsoft.alm.plugin.idea.resources.TfPluginBundle;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class LoginPageModelImpl extends AbstractModel implements LoginPageModel {
    private boolean connected = false;
    private boolean authenticating = false;
    //default values for Strings should be "" rather than null.
    private String userName = "";
    private String serverName = "";
    private PageModel pageModel;

    public LoginPageModelImpl(final PageModel pageModel) {
        this.pageModel = pageModel;
    }

    /**
     * Generates a new server context with session token information for VSO and saves it as the active context
     *
     * @param context
     * @return
     */
    public ServerContext completeSignIn(final ServerContext context) {
        if (context.getType() == ServerContext.Type.VSO_DEPLOYMENT) {
            //generate PAT
            final ServerContext newContext = ServerContextManager.getInstance().createVsoContext(
                    context,
                    VsoAuthenticationProvider.getInstance(),
                    TfPluginBundle.message(TfPluginBundle.KEY_PAT_TOKEN_DESC));
            ServerContextManager.getInstance().setActiveContext(newContext);
            return newContext;
        } else {
            ServerContextManager.getInstance().setActiveContext(context);
            return context;
        }
    }

    @Override
    public void gotoLink(final String url) {
        if (StringUtils.isNotEmpty(url)) {
            BrowserUtil.browse(url);
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void setConnected(final boolean connected) {
        if (this.connected != connected) {
            this.connected = connected;
            setChangedAndNotify(PROP_CONNECTED);
        }
    }

    @Override
    public void signOut() {
        setAuthenticating(false);
        setConnected(false);
        setServerName("");
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(final String userName) {
        if (!StringUtils.equals(this.userName, userName)) {
            this.userName = userName;
            setChangedAndNotify(PROP_USER_NAME);
        }
    }

    @Override
    public boolean isAuthenticating() {
        return authenticating;
    }

    @Override
    public void setAuthenticating(final boolean authenticating) {
        if (this.authenticating != authenticating) {
            this.authenticating = authenticating;
            setChangedAndNotify(PROP_AUTHENTICATING);
        }
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public void setServerName(final String serverName) {
        if (!StringUtils.equals(this.serverName, serverName)) {
            final String newServerName;
            // Allow just the server name as a short hand
            if (StringUtils.isNotEmpty(serverName) && !StringUtils.contains(serverName, UrlHelper.URL_SEPARATOR)
                    && !StringUtils.equals(serverName, TfPluginBundle.message(TfPluginBundle.KEY_USER_ACCOUNT_PANEL_VSO_SERVER_NAME))) {
                // no slash and not "Microsoft Account" means it must just be a onpremise TFS server name, so add all the normal stuff
                newServerName = String.format(DEFAULT_SERVER_FORMAT, serverName);
            } else {
                newServerName = serverName;
            }
            setServerNameInternal(newServerName);
        }
    }

    /**
     * This method allows the derived classes to directly set the server name without the normal setServerName
     * method changing it.
     */
    protected void setServerNameInternal(final String serverName) {
        this.serverName = serverName;
        setChangedAndNotify(PROP_SERVER_NAME);
    }

    @Override
    public void addError(final ModelValidationInfo error) {
        if (pageModel != null) {
            pageModel.addError(error);
        }
    }

    @Override
    public void clearErrors() {
        if (pageModel != null) {
            pageModel.clearErrors();
        }
    }

    @Override
    public List<ModelValidationInfo> getErrors() {
        if (pageModel != null) {
            return pageModel.getErrors();
        }

        return Collections.unmodifiableList(new ArrayList<ModelValidationInfo>());
    }

    @Override
    public boolean hasErrors() {
        if (pageModel != null) {
            return pageModel.hasErrors();
        }

        return false;
    }

    @Override
    public ModelValidationInfo validate() {
        if (!isConnected()) {
            //We should never get here in the UI since "Clone/Import" is disabled unless the user is connected
            //Leaving the extra check here for safety in case the "Clone/Import" gets enabled for some other reason
            return ModelValidationInfo.createWithResource(PROP_CONNECTED,
                    TfPluginBundle.KEY_LOGIN_FORM_ERRORS_NOT_CONNECTED);
        }

        return pageModel.validate();
    }
}
