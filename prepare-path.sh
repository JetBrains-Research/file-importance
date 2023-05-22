#!/usr/bin/env bash

set -e

if [ $# -ne "1" ]; then
  echo "usage: run <repository path>"
  exit 1
fi

ROOT_DIRRECTORY="$(pwd)"
repositoryPath="$1"
projectPath="$(cd "$(dirname "$1")"; pwd)/$(basename "$1")"

#Folder names
graphMiner="DependencyGraph"
BFCalculator="Truck-Factor"
jetbrainsBFCalculator="risky-patterns-idea"
graphAnalyzer="DependencyGraphAnalysis"

#Generate Avelino files
cd "$ROOT_DIRRECTORY/$BFCalculator/gittruckfactor/scripts"
./linguist_script.sh "$projectPath"
./commit_log_script.sh "$projectPath"

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