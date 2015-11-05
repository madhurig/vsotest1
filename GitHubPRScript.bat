REM some changes here

IF "%1" == "" (
 echo "Master repository URL is not provided"
 GOTO :END
)

IF "%2" == "" (
 echo "Mirror repository fork URL is not provided"
 GOTO :END
)

SET masterRepoUrl="%1"
SET mirrorDirName="repoMirror"
SET mirrorRepoForkUrl="%2"

echo "Delete local mirror directory"
rd /S /Q %mirrorDirName%

echo "Clone mirror repository to local mirror directory"
git clone %mirrorRepoForkUrl% %mirrorDirName%

cd %mirrorDirName%

echo "Add a upstream remote to master repository remote"
git remote add upstream %masterRepoUrl%

echo "Fetch latest from master repo"
git fetch upstream

echo "Checkout fork's local master"
git checkout master

echo "merge changes from master to fork's local. Should we do a local branch here?"
git merge upstream/master

echo "Resolve merge conflicts if any, run builds, unit tests and any appropriate manual tests"

echo "Push changes to the master repository"
git push upstream

echo "Delete local mirror directory"
cd ..
rd /S /Q %mirrorDirName%

:END

