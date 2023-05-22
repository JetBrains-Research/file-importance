#!/usr/bin/env bash

set -e

if [ $# -ne "4" ]; then
  echo "usage: run <repository owner> <repository name> <github token> <reset head to YYYY-MM-DD>"
  exit 1
fi

ROOT_DIRRECTORY="$(pwd)"
repositoryOwner="$1"
repositoryName="$2"
githubToken="$3"
resetDate="$4"

#Folder names
graphMiner="DependencyGraph"
BFCalculator="Truck-Factor"
jetbrainsBFCalculator="risky-patterns-idea"
graphAnalyzer="DependencyGraphAnalysis"
repositories="repositories"

mkdir -p "$repositories"

projectPath="$ROOT_DIRRECTORY/$repositories/$repositoryName"
jetbrainsMergeOutput="$projectPath/jetbrains_merge.json"
avelinoMergeOutput="$projectPath/avelino_alias.txt"
usersSavePath="$projectPath/users.json"

#Clone the repo
if [ ! -d "$projectPath" ]
then
  git clone "https://github.com/$repositoryOwner/$repositoryName.git" "$projectPath"
fi

#Reset head to specific date
cd "$projectPath"
lastCommitHash=$(git log --before="$resetDate" --pretty=format:'%H' -n 1)
git reset --hard "$lastCommitHash"

#Generate Avelino files
cd "$ROOT_DIRRECTORY/$BFCalculator/gittruckfactor/scripts"
./linguist_script.sh "$projectPath"
./commit_log_script.sh "$projectPath"

#Merge similar developers
if [ ! -f "$usersSavePath" ]
then
  cd "$ROOT_DIRRECTORY/$graphAnalyzer"
  python3 ./src/DeveloperIdentifier.py -g "$githubToken" -o "$repositoryOwner" -n "$repositoryName" -l "$projectPath" -j "$jetbrainsMergeOutput" -a "$avelinoMergeOutput" -u "$usersSavePath"
fi

#Install required python libraries
cd "$ROOT_DIRRECTORY/$graphAnalyzer"
pip3 install -r requirements.txt


#Prepare project for headless mode
cd "$ROOT_DIRRECTORY/$graphMiner"
"./gradlew" --stacktrace importProject -Pprojectpath="$projectPath"

if [ -d "$ROOT_DIRRECTORY/$jetbrainsBFCalculator" ] 
then
  cd "$ROOT_DIRRECTORY/$jetbrainsBFCalculator"
  "./gradlew" --stacktrace importProject -Pprj="$projectPath"
else
  echo "Can not find JetBrains plugin"
fi