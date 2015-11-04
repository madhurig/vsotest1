IF "%1" == "" (
 echo "Master repository URL is not provided"
 GOTO :END
)

IF "%2" == "" (
 echo "Mirror repository URL is not provided"
 GOTO :END
)

SET masterRepoUrl="%1"
SET mirrorDirName="repoMirror"
SET mirrorRepoUrl="%2"

echo "Delete local mirror directory"
rd /S /Q %mirrorDirName%

echo "Clone mirror repository to local mirror directory"
git clone %mirrorRepoUrl% %mirrorDirName%

cd %mirrorDirName%

echo "Set remote push url to the master repository remote"
git remote set-url origin %masterRepoUrl%

echo "Pull latest"
git pull

echo "Push to the master repository"
git push origin

:END
