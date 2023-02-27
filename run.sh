#!/usr/bin/env bash

set -e

if [ $# -ne "4" ]; then
  echo "usage: run <path to local repository> <repository owner> <repository name> <github token>"
  exit 1
fi

ROOT_DIRRECTORY="$(pwd)"
projectPath="$(cd "$(dirname "$1")"; pwd)/$(basename "$1")"
repositoryOwner="$2"
repositoryName="$3"
githubToken="$4"
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
jetbrainsBFResult="$outputFolderPath/jetbrainsBFResults.json"
avelinoBFResult="$outputFolderPath/avelinoBFResults.json"
authorshipPath="$outputFolderPath/authorships.json"
jetbrainsMergeOutput="$outputFolderPath/jetbrains_merge.json"
avelinoMergeOutput="$outputFolderPath/avelino_alias.txt"
usersSavePath="$outputFolderPath/users.json"
avelinoMergeFilePath="$ROOT_DIRRECTORY/$BFCalculator/gittruckfactor/repo_info/alias.txt"
jetbrainsMergeFilePath="$projectPath/merged_emails.json"

"./$graphMiner/gradlew" -p "./$graphMiner" extractDependencies -Pprojectpath="$projectPath" -Pgraphpath="$graphFilePath" -Ptargetdirectories="$targetDirectoriesPath"


cd "$ROOT_DIRRECTORY/$graphAnalyzer"
pip3 install -r requirements.txt
python3 ./src/DependencyGraphEvaluator.py -g "$graphFilePath" -i "$graphImagePath" -f "$featuresFilePath"
python3 ./src/DeveloperIdentifier.py -g "$githubToken" -o "$repositoryOwner" -n "$repositoryName" -l "$projectPath" -j "$jetbrainsMergeOutput" -a "$avelinoMergeOutput" -u "$usersSavePath"
rm -f "$avelinoMergeFilePath" "$jetbrainsMergeFilePath"
cp "$avelinoMergeOutput" "$avelinoMergeFilePath"
cp "$jetbrainsMergeOutput" "$jetbrainsMergeFilePath"


cd "$ROOT_DIRRECTORY/$BFCalculator/gittruckfactor/scripts";
./linguist_script.sh "$projectPath"
./commit_log_script.sh "$projectPath"
cd ..
mvn package exec:java -Dexec.mainClass="aserg.gtf.GitTruckFactor" -Dexec.args="$projectPath $featuresFilePath $targetDirectoriesPath $avelinoBFResult"


# Jetbrains BF Calculation
cd "$ROOT_DIRRECTORY/$jetbrainsBFCalculator"
"./gradlew" --stacktrace sigExport -Pprj="$projectPath" -Pout="$jetbrainsBFResult" -Ptarget="$targetDirectoriesPath" -Psig="$featuresFilePath" -Pauthorship="$authorshipPath"


cd "$ROOT_DIRRECTORY/$graphAnalyzer"
python3 ./src/OutputEvaluation.py "$resultsPath" "$authorshipPath" "$avelinoBFResult" "$jetbrainsBFResult"

