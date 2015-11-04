IF "%1" == "" (
 echo "Master repository URL is not provided"
 GOTO :END
)

IF "%2" == "" (
 echo "Mirror repository URL is not provided"
 GOTO :END
)

SET masterRepoUrl=%1
SET mirrorDirName="vso-intelliJ-mirror"
SET mirrorRepoUrl=%2

echo "Delete local mirror directory"
rd /S /Q %mirrorDirName%

echo "Clone master repository to local mirror directory"
git clone --mirror %masterRepoUrl% %mirrorDirName%

cd %mirrorDirName%

echo "Set remote push url to the remote mirror repository"
git remote set-url --push origin %mirrorRepoUrl%

echo "Fetch and Prune refs from master repository"
git fetch -p origin

echo "Push to the mirror repository"
git push --mirror

:END
