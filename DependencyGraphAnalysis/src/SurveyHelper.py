import json
import os

import pandas as pd
from os.path import isfile
import numpy as np
from src.utils import write_merged, find_latest_output
from datetime import datetime

import xlsxwriter


class Author:
    def __init__(self, names, emails, logins, primary_email):
        self.names = names
        self.emails = emails
        self.logins = logins
        self.primary_email = primary_email
        self.folders = None
        self.root_authorship = 0.0

    def __str__(self):
        return f"{self.primary_email}:{self.root_authorship}"

    def __repr__(self):
        return f"{self}"


def add_to_dict(dictionary, key, value):
    if key not in dictionary:
        dictionary[key] = []

    dictionary[key] += [value]




allAuthors = pd.read_csv("../data/author_emails_new.csv")
green_emails = pd.read_csv("../data/rsch-4189_part2.csv")

green_emails_projects = {}
for green_email in green_emails.values:
    for i in range(len(allAuthors.values)):
        author_email = allAuthors.values[i][1]
        if author_email == green_email:
            add_to_dict(green_emails_projects, allAuthors.values[i][0], author_email)

project_authors = {}
output_folders = os.listdir("../../outputs")

for project_name in green_emails_projects.keys():
    users_file = f"../../repositories/{project_name}/users.json"

    if isfile(users_file):
        with open(users_file) as f:
            users = json.load(f)

        for user_list in users:
            for u in user_list:
                primary_email = u.split(":")[1]
                if primary_email in green_emails_projects[project_name]:
                    users_info = [uu.split(":") for uu in user_list]
                    names = list(set([ui[0] for ui in users_info if ui[0] != "None"]))
                    emails = list(set([ui[1] for ui in users_info if ui[1] != "None"]))
                    logins = list(set([ui[2] for ui in users_info if ui[2] != "None"]))
                    author = Author(names, emails, logins, primary_email)
                    add_to_dict(project_authors, project_name, author)
                    break

    latest_output_path = find_latest_output(output_folders, project_name)
    if latest_output_path is not None:
        jetbrains_file = f"../../outputs/{latest_output_path}/jetbrainsBFResults.json"
        folders_info = {}
        if isfile(jetbrains_file):
            with open(jetbrains_file) as f:
                jetbrains = json.load(f)
            for info in jetbrains:
                file_count = next(i["totalFiles"] for i in info["info"] if i["significanceIndicator"] == "jetbrains")
                bf_values = [i["busFactor"] for i in info["info"]]
                folders_info[info["path"]] = (file_count, np.var(bf_values), ",".join(map(str, bf_values)))

        authorship_file = f"../../outputs/{latest_output_path}/authorships.json"
        if isfile(authorship_file):
            with open(authorship_file) as f:
                authorships = json.load(f)

            for a in authorships:
                for author in project_authors[project_name]:
                    if a["email"] == author.primary_email:
                        folders = [(f["folderPath"][1:], f["coverage"], folders_info[f["folderPath"]][0],
                                    folders_info[f["folderPath"]][1], folders_info[f["folderPath"]][2]) for f in
                                   a["folders"]]
                        author.folders = folders
                        author.root_authorship = next(f[1] for f in folders if f[0] == "")

                project_authors[project_name].sort(key=lambda x: x.root_authorship, reverse=True)

# print(project_authors)
# for p in project_authors:
#     more_folder_count = 0
#     for a in project_authors[p]:
#         if len(a.folders) > 30:
#             more_folder_count += 1
#     print(f"{p}: {more_folder_count}")


workbook = xlsxwriter.Workbook(f"../out/survey_info{datetime.now().strftime('%d-%m-%Y-%H:%M:%S')}.xlsx")
sheet = workbook.add_worksheet("survey")
for i in range(10):
    sheet.set_column(i, i, 20)

sheet.set_column(5, 5, 100)

sheet.write(0, 0, "Project Name")
sheet.write(0, 1, "Names")
sheet.write(0, 2, "Emails")
sheet.write(0, 3, "Logins")
sheet.write(0, 4, "Authorship")
sheet.write(0, 5, "Path")
sheet.write(0, 6, "Authorship")
sheet.write(0, 7, "File Count")
sheet.write(0, 8, "BF Variance")
sheet.write(0, 9, "BF Values")

row = 1
for project in project_authors:
    for author in project_authors[project]:
        first_row = row
        for folder in author.folders:
            sheet.write(row, 5, folder[0])
            sheet.write(row, 6, "%.5f" % folder[1])
            sheet.write(row, 7, folder[2])
            sheet.write(row, 8, "%.5f" % folder[3])
            sheet.write(row, 9, folder[4])
            row += 1
        write_merged(sheet, first_row, row - 1, 0, project)
        write_merged(sheet, first_row, row - 1, 1, "\n".join(author.names))
        write_merged(sheet, first_row, row - 1, 2, "\n".join(author.emails))
        write_merged(sheet, first_row, row - 1, 3, "\n".join(author.logins))
        write_merged(sheet, first_row, row - 1, 4, "%.5f" % author.root_authorship)

workbook.close()
