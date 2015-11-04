// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.vcs.log.VcsFullCommitDetails;
import com.intellij.vcs.log.VcsLog;
import com.intellij.vcs.log.VcsLogDataKeys;
import com.microsoft.alm.common.utils.UrlHelper;
import com.microsoft.alm.plugin.idea.resources.Icons;
import com.microsoft.alm.plugin.idea.resources.TfPluginBundle;
import com.microsoft.alm.plugin.idea.utils.TfGitHelper;
import git4idea.GitUtil;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OpenCommitInBrowserAction extends InstrumentedAction {

    private static final Logger logger = LoggerFactory.getLogger(OpenCommitInBrowserAction.class);

    public OpenCommitInBrowserAction() {
        super(TfPluginBundle.message(TfPluginBundle.KEY_ACTIONS_OPEN_BROWSER),
                TfPluginBundle.message(TfPluginBundle.KEY_ACTIONS_OPEN_BROWSER_MSG),
                Icons.VSLogoSmall);
    }

    @Override
    public void doUpdate(@NotNull final AnActionEvent anActionEvent) {

        final Presentation presentation = anActionEvent.getPresentation();

        final Project project = anActionEvent.getData(CommonDataKeys.PROJECT);
        final VcsLog log = anActionEvent.getData(VcsLogDataKeys.VCS_LOG);
        if (project == null || project.isDisposed() || log == null) {
            presentation.setEnabledAndVisible(false);
            return;
        }

        final List<VcsFullCommitDetails> commits = log.getSelectedDetails();
        if (commits.size() == 0) {
            presentation.setEnabledAndVisible(false);
            return;
        }

        final VcsFullCommitDetails commit = commits.get(0);

        final GitRepository repository = GitUtil.getRepositoryManager(project).getRepositoryForRoot(commit.getRoot());

        if (repository == null || !TfGitHelper.isTfGitRepository(repository)) {
            presentation.setEnabledAndVisible(false);
            return;
        } else if (commits.size() > 1) {
            // only one for now, leave it visible as a breadcrumb
            presentation.setVisible(true);
            presentation.setEnabled(false);
            return;
        }

        presentation.setEnabledAndVisible(true);
    }

    @Override
    public void doActionPerformed(@NotNull final AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
        final VcsFullCommitDetails commit = anActionEvent.getRequiredData(VcsLogDataKeys.VCS_LOG).getSelectedDetails().get(0);

        final GitRepository gitRepository = GitUtil.getRepositoryManager(project).getRepositoryForRoot(commit.getRoot());
        final GitRemote remote = TfGitHelper.getTfGitRemote(gitRepository);

        // guard for null so findbugs doesn't complain
        if (remote == null) {
            return;
        }

        final String remoteUrl = remote.getFirstUrl();
        if (remoteUrl == null) {
            return;
        }

        final StringBuilder stringBuilder = new StringBuilder(remoteUrl);
        // vso will redirect to /commits (which shows the full history)if the particular commit does not exist on vso
        stringBuilder.append("/commit/");
        stringBuilder.append(commit.getId().asString());
        final String urlToBrowseTo = stringBuilder.toString();
        if (UrlHelper.isValidServerUrl(urlToBrowseTo)) {
            logger.info("Browsing to url " + urlToBrowseTo);
            BrowserUtil.browse(urlToBrowseTo);
        } else {
            logger.warn("Invalid server Url to browse to " + urlToBrowseTo);
        }
    }
}
