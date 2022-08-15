# file-importance
ICTL + BILSEN project on using file importance in risk assessment

## Description
This projects aim to provide a novel algorithm to rank files based on their significance using dependency graph analysis. It utilizes the IntelliJ idea plugin to mine the dependencies and the NetworkX framework to evaluate the dependency graph.

## Structure
There are two subprojects in this repository
1. IntelliJ plugin to mine dependencies under DependencyGraph folder
2. Python code to evaluate the graph under DependencyGraphAalysis folder

You can find all results under output folder after execution

## How to run
### Requirements
- Any type of unix shell
- JDK 11
- Python 3 (Suggested python 3.10)

### How to launch
1. Clone a java project somewhere in your machine (It is suggested to keep it out of this project folders)
2. Create a python virtual environment in the DependencyGraphAnalysis folder
3. Run the following command
```
sh run.sh CLASS <path-to-project>
```
4. You will find the produced files in the output folder