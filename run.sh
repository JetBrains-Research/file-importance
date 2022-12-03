#!/usr/bin/env bash

set -e

if [ $# -ne "1" ]; then
  echo "usage: run <path to project>"
  exit 1
fi

ROOT_DIRRECTORY="$(pwd)"
projectPath="$(cd "$(dirname "$1")"; pwd)/$(basename "$1")"
graphMiner="DependencyGraph"
graphAnalyzer="DependencyGraphAnalysis"
BFCalculator="Truck-Factor"
jetbrainsBFCalculator="risky-patterns-idea"
outputs="outputs"
outputFolderName="$outputs/$(basename $projectPath)-$(date '+%Y-%m-%d-%H:%M')"
# rm -rf "$outputFolderName" 
mkdir -p "$outputs"
mkdir "$outputFolderName"

outputFolderPath="$ROOT_DIRRECTORY/$outputFolderName"

graphFilePath="$outputFolderPath/graph.csv"
graphImagePath="$outputFolderPath/graph.png"
featuresFilePath="$outputFolderPath/features.csv"
targetDirectoriesPath="$outputFolderPath/targets.txt"
resultsPath="$outputFolderPath/results.xlsx"
allDevelopersPath="$outputFolderPath/allDev.txt"
developerAliasesPath="$outputFolderPath/devAlias.txt"
jetbrainsBFResult="$outputFolderPath/jetbrainsBFResults.json"
avelinoBFResult="$outputFolderPath/avelinoBFResults.json"



"./$graphMiner/gradlew" -p "./$graphMiner" extractDependencies -Pprojectpath="$projectPath" -Pgraphpath="$graphFilePath" -Ptargetdirectories="$targetDirectoriesPath"

cd "$projectPath"
git log --pretty="%an;%ae" | sort | uniq > "$allDevelopersPath"

cd "$ROOT_DIRRECTORY/$graphAnalyzer"
pip3 install -r requirements.txt
python3 ./src/DependencyGraphEvaluator.py "$graphFilePath" "$graphImagePath" "$featuresFilePath"
python3 ./src/DeveloperIdentifier.py "$allDevelopersPath" "$developerAliasesPath"
rm -f "$ROOT_DIRRECTORY/$BFCalculator/gittruckfactor/repo_info/alias.txt"
cp "$developerAliasesPath" "$ROOT_DIRRECTORY/$BFCalculator/gittruckfactor/repo_info/alias.txt"

cd "$ROOT_DIRRECTORY/$BFCalculator/gittruckfactor/scripts";
./linguist_script.sh "$projectPath"
./commit_log_script.sh "$projectPath"
cd ..
mvn package exec:java -Dexec.mainClass="aserg.gtf.GitTruckFactor" -Dexec.args="$projectPath $featuresFilePath $targetDirectoriesPath $avelinoBFResult"


# Jetbrains BF Calculation
cd "$ROOT_DIRRECTORY/$jetbrainsBFCalculator"
"./gradlew" sigExport -Pprj="$projectPath" -Pout="$jetbrainsBFResult" -Ptarget="$targetDirectoriesPath" -Psig="$featuresFilePath"


cd "$ROOT_DIRRECTORY/$graphAnalyzer"
python3 ./src/OutputEvaluation.py "$resultsPath" "$avelinoBFResult" "$jetbrainsBFResult"

