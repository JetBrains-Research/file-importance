#!/usr/bin/env bash
if [ $# -ne "3" ]; then
  echo "usage: run <dependency level> <path to project> <path to output folder>"
  exit 1
fi

projectPath="$(cd "$(dirname "$2")"; pwd)/$(basename "$2")"
sharedFolder="$(cd "$(dirname "$3")"; pwd)/$(basename "$3")"

graphMiner="DependencyGraph"

"./$graphMiner/gradlew" -p "./$graphMiner" extractDependencies -Pdeplevel="$1" -Pprojectpath="$projectPath" -Poutputpath="$sharedFolder"
