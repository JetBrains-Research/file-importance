#!/usr/bin/env bash

set -e

if [ $# -ne "2" ]; then
  echo "usage: run <dependency level> <path to project> <repository full name>"
  exit 1
fi

ROOT_DIRRECTORY="$(pwd)"
projectPath="$(cd "$(dirname "$2")"; pwd)/$(basename "$2")"
graphMiner="DependencyGraph"
graphAnalyzer="DependencyGraphAnalysis"
BFCalculator="Truck-Factor"
outputFolderName="output"
rm -rf "$outputFolderName" 
mkdir "$outputFolderName"

outputFolderPath="$ROOT_DIRRECTORY/$outputFolderName"

graphFilePath="$outputFolderPath/graph.json"
infoFilePath="$outputFolderPath/info.json"
graphImagePath="$outputFolderPath/graph.png"
featuresFilePath="$outputFolderPath/features.csv"


"./$graphMiner/gradlew" -p "./$graphMiner" extractDependencies -Pdeplevel="$1" -Pprojectpath="$projectPath" -Pgraphpath="$graphFilePath" -Pinfopath="$infoFilePath"


cd "$graphAnalyzer"
pip3 install -r requirements.txt
python3 ./src/DependencyGraphEvaluator.py "$graphFilePath" "$graphImagePath" "$featuresFilePath"

cd "$ROOT_DIRRECTORY/$BFCalculator/gittruckfactor/scripts";
./commit_log_script.sh "$projectPath"
cd ..
mvn package exec:java -Dexec.mainClass="aserg.gtf.GitTruckFactor" -Dexec.args="$projectPath Null $featuresFilePath"



