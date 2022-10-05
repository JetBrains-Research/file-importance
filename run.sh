#!/usr/bin/env bash

set -e

if [ $# -ne "2" ]; then
  echo "usage: run <dependency level> <path to project>"
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

graphFilePath="$outputFolderPath/graph.csv"
infoFilePath="$outputFolderPath/info.json"
graphImagePath="$outputFolderPath/graph.png"
featuresFilePath="$outputFolderPath/features.csv"
targetDirectoriesPath="$outputFolderPath/targets.txt"
resultsPath="$outputFolderPath/results.xlsx"
allDevelopersPath="$outputFolderPath/allDev.txt"
developerAliasesPath="$outputFolderPath/devAlias.txt"



"./$graphMiner/gradlew" -p "./$graphMiner" extractDependencies -Pdeplevel="$1" -Pprojectpath="$projectPath" -Pgraphpath="$graphFilePath" -Pinfopath="$infoFilePath" -Ptargetdirectories="$targetDirectoriesPath"

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
mvn package exec:java -Dexec.mainClass="aserg.gtf.GitTruckFactor" -Dexec.args="$projectPath $featuresFilePath $targetDirectoriesPath $resultsPath"



