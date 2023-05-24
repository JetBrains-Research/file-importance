#!/usr/bin/env bash

set -e

if [ $# -ne "1" ]; then
  echo "usage: run <path to local repository>"
  exit 1
fi

echo "$(pwd)"

ROOT_DIRRECTORY="$(pwd)"
projectPath="$(cd "$(dirname "$1")"; pwd)/$(basename "$1")"
graphMiner="DependencyGraph"
graphAnalyzer="DependencyGraphAnalysis"
BFCalculator="ABF"
jetbrainsBFCalculator="JBF"
outputFolderName="output-test"
rm -rf "$outputFolderName" 
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
specialsFilePath="$projectPath/specials.txt"

#Extract dependency graph
cd "$ROOT_DIRRECTORY/$graphMiner"
"./gradlew" extractDependencies -Pprojectpath="$projectPath" -Pgraphpath="$graphFilePath" -Ptargetdirectories="$targetDirectoriesPath" -Pspecials="$specialsFilePath"

#Generate feature vector
cd "$ROOT_DIRRECTORY/$graphAnalyzer"
python3 ./src/DependencyGraphEvaluator.py -g "$graphFilePath" -i "$graphImagePath" -f "$featuresFilePath"

#Run avelino's tool
cd "$ROOT_DIRRECTORY/$BFCalculator/gittruckfactor"
rm -f "$avelinoMergeFilePath"
touch "$avelinoMergeFilePath"
mvn package exec:java -Dexec.mainClass="aserg.gtf.GitTruckFactor" -Dexec.args="$projectPath $featuresFilePath $targetDirectoriesPath $avelinoBFResult"


# Jetbrains BF Calculation
cd "$ROOT_DIRRECTORY/$jetbrainsBFCalculator"
if [ -f "$jetbrainsMergeFilePath" ]
then
  mv -f "$jetbrainsMergeFilePath" "$jetbrainsMergeFilePath-tmp"
fi

if [ ! -z "$(ls -A)" ] 
then
  "./gradlew" --stacktrace sigExport -Pprj="$projectPath" -Pout="$jetbrainsBFResult" -Ptarget="$targetDirectoriesPath" -Psig="$featuresFilePath" -Pauthorship="$authorshipPath"
else
  echo "Can not find JetBrains plugin"
fi

if [ -f "$jetbrainsMergeFilePath-tmp" ]
then
  mv -f "$jetbrainsMergeFilePath-tmp" "$jetbrainsMergeFilePath"
fi

#Generate resuts
cd "$ROOT_DIRRECTORY/$graphAnalyzer"
python3 ./src/OutputEvaluation.py -o "$resultsPath" -a "$authorshipPath" -s "$specialsFilePath" -i "$avelinoBFResult" "$jetbrainsBFResult"

