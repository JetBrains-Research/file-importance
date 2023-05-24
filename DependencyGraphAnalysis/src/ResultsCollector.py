import json
import os
from os.path import isfile
from datetime import datetime
import pandas as pd
import xlsxwriter

from src.OutputEvaluation import merge_inputs, load_inputs
from src.utils import find_latest_output

datapoints = pd.read_csv("../data/datapoints.csv")
project_names = list(set([dp[0] for dp in datapoints.values]))
output_folders = os.listdir("../../outputs")

projects = {}
for project_name in project_names:
    latest_output_name = find_latest_output(output_folders, project_name)
    if latest_output_name is None:
        raise Exception(f"Can not find output for project {project_name}")

    jetbrains_file = f"../../outputs/{latest_output_name}/jetbrainsBFResults.json"
    avelino_file = f"../../outputs/{latest_output_name}/avelinoBFResults.json"
    if not isfile(jetbrains_file) or not isfile(avelino_file):
        raise Exception(f"Can not find the BFResults files for project {project_name}")

    inputs = load_inputs([avelino_file, jetbrains_file])
    projects[project_name] = merge_inputs(inputs)

info_order = [
    ("ave-Avelino", "ave"),
    ("ave-pageRank", "ave-pg"),
    ("ave-degreeCentrality", "ave-dc"),
    ("ave-inDegreeCentrality", "ave-idc"),
    ("ave-outDegreeCentrality", "ave-odc"),
    ("ave-betweennessCentrality", "ave-bc"),
    ("jet-jetbrains", "jet"),
    ("jet-pageRank", "jet-pg"),
    ("jet-degreeCentrality", "jet-dc"),
    ("jet-inDegreeCentrality", "jet-idc"),
    ("jet-outDegreeCentrality", "jet-odc"),
    ("jet-betweennessCentrality", "jet-bc")
]

results = []
for dp in datapoints.values:
    if dp[1] == "Whole Project":
        dp[1] = ""

    project_data = projects[dp[0]]
    result = [dp[0], dp[1]]

    path_data = next((d for d in project_data if d["path"] == dp[1]), None)
    if path_data == None:
        print(f"Can not find data for {dp}")
    else:
        for io in info_order:
            result += [next((i["busFactor"] for i in path_data["info"] if i["significanceIndicator"] == io[0]), -1)]

    results += [result]

workbook = xlsxwriter.Workbook(f"../out/Datapoints_Results_{datetime.now().strftime('%d-%m-%Y-%H:%M:%S')}.xlsx")
sheet = workbook.add_worksheet("DataPoints")


sheet.write(0, 0, "Project Name")
sheet.write(0, 1, "Path")
for i, io in enumerate(info_order):
    sheet.write(0, i+2, io[1])

for row, r in enumerate(results):
    for column, rr in enumerate(r):
        sheet.write(row+1, column, f"{rr}")

workbook.close()
