# BFSig
BFSig is a Bus Factor(BF) estimator. The current version of BFSig is optimized to estimate BF for Java and Kotlin projects. However, it can evaluate almost all programming languages with the correct configuration.

BFSig is sponsered by JetBrains Research and Bilkent University.

## Description
BFSig is a mutation of state-of-the-art BF estimators to weight the influence of different files according to their topological significance extracted from the project's dependency graph. In addition to the whole project, BFSig evaluates BF information for subfolders of a given repository.

BFSig utilizes IntelliJ IDEA for dependency graph extraction and NetworkX to evaluate PageRank, In-/Out-/All-Degree, and Betweenness centralities for files in the extracted graph.

Currently, BFSig uses two baseline tools provided by [Avelino et al.](https://github.com/aserg-ufmg/Truck-Factor) (ABF) and [Jabrayilzade et al.](https://dl.acm.org/doi/abs/10.1145/3510457.3513082) (JBF). Both tools are added as submodules to this repository. However, the JBF is not publicly available and is licensed to JetBrains. To access JBF's source code, contact [vovak](https://github.com/vovak).


## Structure
1. ***DependencyGraph:*** IntelliJ plugin to mine all references between files and export Dependency graph.
1. ***DependencyGraphAnalysis:*** Python codes to evaluate file's significance scores. This folder contains many helper scripts to merge authors, aggregate results, automate survey generation and so on.
1. ***ABF:*** Modified tool provided by Avelino.
1. ***JBF:*** Modified tool provided by Jabrayilzade.
1. ***Evaluations:*** This folder contains an Excel sheet which compares the quality of BFSig estimates with the state-of-the-art methods. Besides you can find generated results for several projects under this folder.
1. ***tests:*** This folder contains a kotlin test project, which evaluate the consistency of the results with previously generated spces.
1. ***SampleProjects*** Seven sample projects which has been used to check the validity of generated dependency graph.
1. ***SampleOutputs*** Few sample outputs are included to provide insights about the structure of generated outputs.

## How to run
To run the evaluation over your repository follow bellow steps:
1. Clone this repository source code with submodules. Look [here](https://git-scm.com/book/en/v2/Git-Tools-Submodules) for details about how to clone submodules. If you have access to JBF place it under `JBF` folder.
1. Prepare the target repository for evaluation.
1. Launch evaluation pipeline.

### Requirements
- **JDK 11+** (17 is suggested)
- **Python 3.6+** (3.10 is suggested)
- Any unix based shell. Automation is provided as **bash** scripts however you can run BFSig manually
- Any tool to open **xlsx** files such as Microsoft Excel, LibreOffice Calc, or Google Sheet

### Importing your project
To run an evaluation of your project, you need to import the project into the IntelliJ instance used by BFSig and let it index your whole project. `prepare.sh` and `prepare-path.sh` help you to do this step. 

After the repository is imported, manual modification might be required to import the project and corresponding modules fully. You might need to set the correct SDK and reload the project based on your parent build script. It automatically resolves all dependencies specified in the maven or gradle configuration. Still, if the repository contains Python, Javascript, or any other programming language, you must manually install the required dependencies for a complete evaluation.

**Make sure everything is imported properly, and you can use the referencing feature of IntelliJ. Building the project and ensuring there is no compile error is recommended. Also, ensure the VCS is configured properly and you can access the git history.**

When the import is finished, close the IntelliJ and wait for the script to finish the preparation. If you are using JBF, the plugin will open the IntelliJ instance used by JBF to import the project. You need to ensure that the VCS is configured properly and you can access the git history in the IDE.

#### Full Preparation
`prepare.sh` helps you to clone your GitHub repository and import it into IntelliJ. 
Following is an example of how to prepare the [elastic/elasticsearch](https://github.com/elastic/elasticsearch) repository for evaluation.

```
./prepare.sh elastic elasticsearch <github-token> 2023-01-01
```
`prepare.sh` takes following steps:
1. Clone the provided repository under `repositories` folder.
1. Query all contributors of the project from github and identify duplicates automatically.
1. Prepare the repository for ABF evaluation.
1. Install all required dependencies such as IntelliJ.
1. Prepare repository for dependency graph extraction.
1. In case you are using JBF prepare repository for JBF evaluation.

To run `prepare.sh` you need to provide following parameters:
1. ***Project Owner:*** This parameter is required to identify your repository on GitHub. It is the name that comes in front of your repository identifier before the "/." For example owner of the elastic/elasticsearch is *elastic*.
2. ***Project Name:*** This parameter along with the previous parameter identifies your repository. 
3. ***Github Token:*** A "Personal GitHub Access Token" is required to start querying the repository's contributors to identify duplicated ones. Look [here](https://docs.github.com/en/enterprise-server@3.4/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) for step-by-step guide to generate personal access token.
4. ***Reset Date:*** You should specify a date on which the script will reset the head of the branch to the latest commit before the specified date. The date should be in *YYYY-MM-DD* format. If you want to run the evaluation for the latest repository version, set the date in the future.

#### Manual Preparation
If your repository is not hosted on GitHub or you can not use full preparation for any reason, you can use `prepare-path.sh`. For instance it should be used to prepare sample projects. Following is an example of how to prepare `reference-test-mixed` project:

```
./prepare-path.sh SampleProjects/reference-test-mixed
```
To run `prepare-path.sh` you need to provide the path to your repository on your local file system. Afterward, the script will open and import the following path into IntelliJ (You can provide relative path).

### Run Evaluation
To run the evaluation you can use `run.sh` which will launch the evaluation pipeline and place the results under `outputs` folder with `<reponame>-<timestamp>` format. Following is an example of how to launch evaluation pipeline:

```
./run.sh SampleProjects/reference-test-java
```

If the execution finishes without error, the following files will be available in the output folder:
1. ***results.xlsx:*** An excel sheet which contains aggregated results for all estimators variants.
1. ***graph.csv:*** Dependency graph of evaluated projects.
1. ***features.csv:*** Files' feature vector, consisting of significance scores according to graph metrics BFSig evaluates.
1. ***targets.txt:*** List of subfolders within the project to evaluate the BF.
1. ***avelinoBFResults.json:*** Results produced by ABF.
1. ***jetbrainsBFResults.json:*** Results produced by JBF.
1. ***authorship.json:*** Authorship information exported from JBF.

### Configuration
Configurations are places in `intellij` specification of `build.gradle.kts` file under DependencyGraph project.
```
version.set("2022.3.2")
    type.set("IU")
    plugins.set(
        listOfNotNull(
            "java",
            "Kotlin",
//            "org.intellij.scala:2022.3.20",
//            "com.jetbrains.php:223.8617.20",
//            "Pythonid:223.8617.20",
//            "org.jetbrains.plugins.ruby:223.8617.56",
//            "org.jetbrains.plugins.go:223.8617.9",
//            "org.jetbrains.erlang:0.11.1162"
        )
    )
```

BFSig is configured to use the IntelliJ Ultimate version. If you don't have a license for the ultimate version of IntelliJ, you can use the trial time. Otherwise, you need to configure BFSig to use the community version of IntelliJ. To do so, you must change "IU" to "IC." 

To run BFSig over repositories containing programming languages other than Java and Kotlin, you need to change the configuration and add corresponding plugins. To do so, you need to add the required plugins (Some are included in comments). Look [here](https://www.jetbrains.com/help/idea/discover-intellij-idea.html#multi-platform-IDE) for details about the supported programming languages.

Furthermore, you can change the product type from IntelliJ to other JetBrains products, such as CLion. Look [here](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html) for more details about available products and configurations.

### Tests
You can use the automated test under the `tests` folder to test if you have set up BFSig correctly. There are several specs included for sample projects. In addition, there is a spec for [sprig cloud alibaba](https://github.com/alibaba/spring-cloud-alibaba) with the last commit of 2022. Look into `tests/specs` folder to check all available specs.

To run the tests first make sure to prepare the target project. Then configure the project name and path in `gradle.properties` under `tests` folder

```
repo.path=SampleProjects/reference-test-mixed
repo.name=reference-test-mixed
```
Finally, run the tests with following command in `tests` folder.
```
./gradlew test
```

**Note that all the specs are generated using the Ultimate version of IntelliJ, and tests might fail if you use the community version.**
