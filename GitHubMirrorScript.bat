rd /S /Q vso-intelliJ-mirror
git clone --mirror %1 vso-intelliJ-mirror
cd vso-intelliJ-mirror
git remote set-url --push origin https://github.com/madhurig/vsotest1.git
git fetch -p origin
git push --mirror
