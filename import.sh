#!/usr/bin/env bash

set -e

if [ $# -ne "1" ]; then
  echo "usage: run <path to project>"
  exit 1
fi

ROOT_DIRRECTORY="$(pwd)"
projectPath="$(cd "$(dirname "$1")"; pwd)/$(basename "$1")"

jetbrainsBFCalculator="risky-patterns-idea"
graphMiner="DependencyGraph"


"./$graphMiner/gradlew" -p "./$graphMiner" importProject -Pprojectpath="$projectPath"
cd "$ROOT_DIRRECTORY/$jetbrainsBFCalculator"
"./gradlew" importProject -Pprj="$projectPath"