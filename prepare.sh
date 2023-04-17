#!/usr/bin/env bash

set -e

if [ $# -ne "3" ]; then
  echo "usage: run <repository owner> <repository name> <github token>"
  exit 1
fi

ROOT_DIRRECTORY="$(pwd)"
repositoryOwner="$1"
repositoryName="$2"
githubToken="$3"

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

#Reset head to the last commit of 2022
cd "$projectPath"
lastCommitHash=$(git log --before="2023-01-01" --pretty=format:'%H' -n 1)
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


#Prepare projects for headless mode
cd "$ROOT_DIRRECTORY"
"./$graphMiner/gradlew" -p "./$graphMiner" importProject -Pprojectpath="$projectPath"

cd "$ROOT_DIRRECTORY/$jetbrainsBFCalculator"
"./gradlew" --stacktrace importProject -Pprj="$projectPath"