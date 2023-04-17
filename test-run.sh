#!/usr/bin/env bash

set -e

if [ $# -ne "1" ]; then
  echo "usage: run <path to local repository>"
  exit 1
fi

ROOT_DIRRECTORY="$(pwd)"
projectPath="$(cd "$(dirname "$1")"; pwd)/$(basename "$1")"
graphMiner="DependencyGraph"
graphAnalyzer="DependencyGraphAnalysis"
BFCalculator="Truck-Factor"
jetbrainsBFCalculator="risky-patterns-idea"
outputs="outputs"
# outputFolderName="$outputs/$(basename $projectPath)-$(date '+%Y-%m-%d-%H:%M')"
outputFolderName="output"
rm -rf "$outputFolderName" 
mkdir -p "$outputs"
mkdir "$outputFolderName"

outputFolderPath="$ROOT_DIRRECTORY/$outputFolderName"

graphFilePath="$outputFolderPath/graph.csv"
graphImagePath="$outputFolderPath/graph.png"
featuresFilePath="$outputFolderPath/features.csv"
targetDirectoriesPath="$outputFolderPath/targets.txt"
resultsPath="$outputFolderPath/results.xlsx"
jetbrainsBFResult="$outputFolderPath/jetbrainsBFResults.json"
avelinoBFResult="$outputFolderPath/avelinoBFResults.json"
authorshipPath="$outputFolderPath/authorships.json"
avelinoMergeFilePath="$ROOT_DIRRECTORY/$BFCalculator/gittruckfactor/repo_info/alias.txt"
jetbrainsMergeFilePath="$projectPath/merged_emails.json"

"./$graphMiner/gradlew" -p "./$graphMiner" extractDependencies -Pprojectpath="$projectPath" -Pgraphpath="$graphFilePath" -Ptargetdirectories="$targetDirectoriesPath"


cd "$ROOT_DIRRECTORY/$graphAnalyzer"
pip3 install -r requirements.txt
python3 ./src/DependencyGraphEvaluator.py -g "$graphFilePath" -i "$graphImagePath" -f "$featuresFilePath"

cd "$ROOT_DIRRECTORY/$BFCalculator/gittruckfactor"
rm -f "$avelinoMergeFilePath"
touch "$avelinoMergeFilePath"
mvn package exec:java -Dexec.mainClass="aserg.gtf.GitTruckFactor" -Dexec.args="$projectPath $featuresFilePath $targetDirectoriesPath $avelinoBFResult"


# Jetbrains BF Calculation
cd "$ROOT_DIRRECTORY/$jetbrainsBFCalculator"
mv -f "$jetbrainsMergeFilePath" "$jetbrainsMergeFilePath-tmp"
"./gradlew" --stacktrace sigExport -Pprj="$projectPath" -Pout="$jetbrainsBFResult" -Ptarget="$targetDirectoriesPath" -Psig="$featuresFilePath" -Pauthorship="$authorshipPath"
mv -f "$jetbrainsMergeFilePath-tmp" "$jetbrainsMergeFilePath"

cd "$ROOT_DIRRECTORY/$graphAnalyzer"
python3 ./src/OutputEvaluation.py -o "$resultsPath" -a "$authorshipPath" -i "$avelinoBFResult" "$jetbrainsBFResult"

