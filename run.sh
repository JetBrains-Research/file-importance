#!/usr/bin/env bash

set -e

if [ $# -ne "2" ]; then
  echo "usage: run <dependency level> <path to project>"
  exit 1
fi

projectPath="$(cd "$(dirname "$2")"; pwd)/$(basename "$2")"

graphMiner="DependencyGraph"
graphAnalyzer="DependencyGraphAnalysis"
outputFolderName="output"
rm -rf "$outputFolderName" 
mkdir "$outputFolderName"

outputFolderPath="$(pwd)/$outputFolderName"

graphFilePath="$outputFolderPath/graph.json"
infoFilePath="$outputFolderPath/info.json"
graphImagePath="$outputFolderPath/graph.png"
featuresFilePath="$outputFolderPath/features.csv"


"./$graphMiner/gradlew" -p "./$graphMiner" extractDependencies -Pdeplevel="$1" -Pprojectpath="$projectPath" -Pgraphpath="$graphFilePath" -Pinfopath="$infoFilePath"


cd "$graphAnalyzer"
pip3 install -r requirements.txt
python3 ./src/DependencyGraphEvaluator.py "$graphFilePath" "$graphImagePath" "$featuresFilePath"
