#!/usr/bin/env bash
if [ $# -ne "2" ]; then
  echo "usage: run <dependency level> <path to project>"
  exit 1
fi

projectPath="$(cd "$(dirname "$2")"; pwd)/$(basename "$2")"

graphMiner="DependencyGraph"
graphAnalyzer="DependencyGraphAnalysis"
outputFolderName="./output"
mkdir "$outputFolderName"

outputFolderPath="$(cd "$(dirname "$outputFolderName")"; pwd)/$(basename "$outputFolderName")"

graphFilePath="$outputFolderPath/graph.json"
infoFilePath="$outputFolderPath/info.json"
graphImagePath="$outputFolderPath/graph.png"

"./$graphMiner/gradlew" -p "./$graphMiner" extractDependencies -Pdeplevel="$1" -Pprojectpath="$projectPath" -Poutputpath="$outputFolderPath"


cd "$graphAnalyzer"
pip3 install -r requirements.txt
python3 ./code/DependencyGraphEvaluator.py "$graphFilePath" "$graphImagePath"