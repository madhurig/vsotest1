	echo Clone the master
	git clone %masterRepoURL% %mirrorDirName%
	cd %mirrorDirName%
	
	echo Fetch the branch in the fork for which the PR was created
	git fetch %forkRepoURL% %forkBranch%:github/pr/%githubPullRequestId%
	
echo Checkout the fork branch and merge changes from master
	git checkout github/pr/%githubPullRequestId%
	git merge master

	echo If there are merge conflicts: Leave a comment on the Pull Request asking user to sync their fork and abandon rest of the process
	
echo If merge succeeds, push changes to master
	git push upstream
	
	echo Create a pull request for the new branch github/pr/%githubPullRequestId%
