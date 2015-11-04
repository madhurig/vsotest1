// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class TfGitHelper {

    /**
     * Returns <code>true</code> if the specified GitRepository is a TF GitRepository (VSO or OnPrem).
     *
     * @param gitRepository must not be <code>null</code>
     * @return
     */
    public static boolean isTfGitRepository(final GitRepository gitRepository) {
        return getTfGitRemote(gitRepository) != null;
    }

    public static GitRemote getTfGitRemote(final GitRepository gitRepository) {
        if (gitRepository == null) {
            throw new IllegalArgumentException();
        }
        GitRemote first = null;
        for (GitRemote gitRemote : gitRepository.getRemotes()) {
            if (isTfGitRemote(gitRemote)) {
                if (gitRemote.getName().equals("origin")) {
                    return gitRemote;
                } else if (first == null) {
                    first = gitRemote;
                }
            }
        }
        return first;
    }

    private static boolean isTfGitRemote(final GitRemote gitRemote) {
        if (gitRemote == null) {
            return false;
        }

        final String remoteUrl = gitRemote.getFirstUrl();
        if (remoteUrl != null && (remoteUrl.contains(".visualstudio.com/") || remoteUrl.contains(".tfsallin.net/") || remoteUrl.contains("/_git/"))) {
            //TODO once we have the connections cached, we should rework this to also query those for better OnPrem detection.
            return true;
        }
        return false;
    }

    public static Collection<GitRemote> getTfGitRemotes(@NotNull final GitRepository gitRepository) {
        assert gitRepository != null;
        Collection<GitRemote> gitRemotes = gitRepository.getRemotes();

        return Collections2.filter(gitRemotes, new Predicate<GitRemote>() {
            @Override
            public boolean apply(final GitRemote remote) {
                return TfGitHelper.isTfGitRemote(remote);
            }
        });
    }
}
