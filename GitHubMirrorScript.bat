rd /S /Q Java.IntelliJ.Mirror
git clone --mirror https://madhurig-msft.visualstudio.com/DefaultCollection/GitMirrorTest/_git/vsomaster Java.IntelliJ.Mirror
cd Java.IntelliJ.Mirror
git remote set-url --push origin https://github.com/madhurig/vsotest1.git
git fetch -p origin
git push --mirror
