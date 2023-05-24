import argparse
import json
import os.path
import sys

import numpy as np
import xlsxwriter
from src.utils import write_merged


def log(msg):
    print(f"****Exporter**** {msg}")


def load_data(path):
    with open(path) as json_file:
        data = json.load(json_file)
        file_name_index = path.rfind("/") + 1
        id = path[file_name_index:file_name_index + 3]
        return {"data": data, "id": id}


def get_args():
    parser = argparse.ArgumentParser(description="Detecting developer duplicates")

    parser.add_argument("-o", "--output_file", type=str, help="output file path", required=True)
    parser.add_argument("-a", "--authorship_file", type=str, help="authorship file path", required=True)
    parser.add_argument("-i", "--input_files", type=str, nargs='+', help="bus factor information input files",
                        required=True)
    parser.add_argument("-s", "--specials_file", type=str, help="special paths file",
                        required=True)

    args = parser.parse_args()
    return args.output_file, args.authorship_file, args.input_files, args.specials_file


def load_inputs(input_files):
    return [load_data(file_path) for file_path in input_files if os.path.isfile(file_path)]


def normalize_path(path):
    if len(path) > 0 and path[0] == "/":
        return path[1:]

    return path


def merge_inputs(inputs):
    for i in inputs:
        for d in i["data"]:
            d["path"] = normalize_path(d["path"])

    paths = [d["path"] for in_data in inputs for d in in_data["data"]]
    paths = set(paths)

    result = []
    for p in paths:
        info = []
        for in_data in inputs:
            inf = next((d["info"] for d in in_data["data"] if d["path"] == p), None)
            if inf is not None:
                for i in inf:
                    i["significanceIndicator"] = f"{in_data['id']}-{i['significanceIndicator']}"

                info += inf
        result += [{"path": p, "info": info}]

    return result


def load_authorship_info(authorship_file):
    if not os.path.isfile(authorship_file):
        return None
    with open(authorship_file) as json_file:
        data = json.load(json_file)
        for d in data:
            for f in d["folders"]:
                f["folderPath"] = normalize_path(f["folderPath"])

        return data


def calculate_disagreement(data):
    indicators_count = len(data[0]["info"])
    for d in data:
        d["disagreement"] = (len(set([i["busFactor"] for i in d["info"]])) - 1) / indicators_count

    return data


def write_authorship_info(workbook, data, authorship_info):
    if authorship_info is None:
        return

    worksheet = workbook.add_worksheet("Survey Suggestions")

    for i in range(2, 5):
        worksheet.set_column(i, i, 20)

    worksheet.set_row(0, 40)
    worksheet.set_row(1, 40)
    worksheet.set_column(0, 0, 40)

    centered_text = workbook.add_format({
        'align': 'center',
        'valign': 'vcenter',
        'font_size': 10})

    left_text_format = workbook.add_format({
        'align': 'left',
        'valign': 'vcenter',
        'font_size': 10})

    header_format = workbook.add_format({
        'bold': 1,
        'align': 'center',
        'valign': 'vcenter',
        'fg_color': '000080',
        'font_color': 'white'})

    worksheet.merge_range(0, 0, 1, 0, "Email", header_format)
    worksheet.merge_range(0, 1, 0, 4, "Folders", header_format)
    worksheet.write(1, 1, "Folder Path", header_format)
    worksheet.write(1, 2, "Score", header_format)
    worksheet.write(1, 3, "Disagreement", header_format)
    worksheet.write(1, 4, "Authorship", header_format)

    disagreement_info = {d["path"]: d["disagreement"] for d in data}

    for a in authorship_info:
        root_folder = next(f for f in a["folders"] if f["folderPath"] == "")
        a["root_coverage"] = root_folder["coverage"]

        for f in a["folders"]:
            f["disagreement"] = disagreement_info[f["folderPath"]]
            f["score"] = (f["disagreement"] + f["coverage"]) / 2

    authorship_info = sorted(authorship_info, key=lambda x: x["root_coverage"], reverse=True)

    row = 2
    longest_path = 0
    for a in authorship_info:
        first_row = row
        sorted_folders = sorted(a["folders"], key=lambda x: x["score"], reverse=True)
        count = len(sorted_folders)
        if count > 20:
            count = 20
        for i in range(count):
            f = sorted_folders[i]
            if len(f["folderPath"]) > longest_path:
                longest_path = len(f["folderPath"])

            path = f["folderPath"]
            if path == "":
                path = "root"

            worksheet.write(row, 1, path, left_text_format)
            worksheet.write(row, 2, "%.4f" % f["score"], centered_text)
            worksheet.write(row, 3, "%.4f" % f["disagreement"], centered_text)
            worksheet.write(row, 4, "%.4f" % f["coverage"], centered_text)
            row = row + 1

        if row - first_row > 1:
            worksheet.merge_range(first_row, 0, row - 1, 0, a["email"], left_text_format)
        else:
            worksheet.write(first_row, 0, a["email"], left_text_format)

    worksheet.set_column(1, 1, longest_path)


def write_BF_Info(workbook, data):
    worksheet = workbook.add_worksheet("All")

    max_path_length = max([len(d["path"]) for d in data])
    max_indicator_length = max([len(i["significanceIndicator"]) for i in data[0]["info"]])
    worksheet.set_column(0, 0, max_path_length)
    worksheet.set_column(1, 1, max_indicator_length)

    for i in range(2, 11):
        worksheet.set_column(i, i, 25)

    worksheet.set_row(0, 40)
    worksheet.set_row(1, 40)

    centered_text = workbook.add_format({
        'align': 'center',
        'valign': 'vcenter',
        'font_size': 10})

    left_text_format = workbook.add_format({
        'align': 'left',
        'valign': 'vcenter',
        'font_size': 10})

    header_format = workbook.add_format({
        'bold': 1,
        'align': 'center',
        'valign': 'vcenter',
        'fg_color': '000080',
        'font_color': 'white'})

    worksheet.write(0, 0, "Path", header_format)
    worksheet.write(0, 1, "Disagreement", header_format)
    worksheet.write(0, 2, "Significance Indicator", header_format)
    worksheet.write(0, 3, "Truck Factor", header_format)
    worksheet.write(0, 4, "Remaining Coverage", header_format)
    worksheet.write(0, 5, "Total File Count", header_format)
    worksheet.merge_range(0, 6, 0, 9, "Developers", header_format)

    worksheet.merge_range(1, 0, 1, 5, "", header_format)
    worksheet.write(1, 6, "Name", header_format)
    worksheet.write(1, 7, "email", header_format)
    worksheet.write(1, 8, "Number of Major Files", header_format)
    worksheet.write(1, 9, "Major Coverage", header_format)

    row = 2
    for d in data:
        path_row = row
        for i in d["info"]:
            indicator_row = row
            for developer in i["developers"]:
                developer_name = developer["name"]
                if developer_name == "":
                    developer_name = "NA"

                worksheet.write(row, 6, developer_name, left_text_format)
                worksheet.write(row, 7, developer["email"], left_text_format)
                worksheet.write(row, 8, developer["numberOfDOAFiles"], centered_text)
                worksheet.write(row, 9, "%.2f" % developer["doaAuthorshipCoverage"], centered_text)
                row += 1

            if indicator_row == row:
                row += 1

            if row - indicator_row > 1:
                worksheet.merge_range(indicator_row, 2, row - 1, 2, i["significanceIndicator"], left_text_format)
                worksheet.merge_range(indicator_row, 3, row - 1, 3, i["busFactor"], centered_text)
                worksheet.merge_range(indicator_row, 4, row - 1, 4, "%.2f" % i["coverage"], centered_text)
                worksheet.merge_range(indicator_row, 5, row - 1, 5, i["totalFiles"], centered_text)
            else:
                worksheet.write(row - 1, 2, i["significanceIndicator"], left_text_format)
                worksheet.write(row - 1, 3, i["busFactor"], centered_text)
                worksheet.write(row - 1, 4, "%.2f" % i["coverage"], centered_text)
                worksheet.write(row - 1, 5, i["totalFiles"], centered_text)

        disagreement_color = hex(200 - int(d["disagreement"] * 128))[2:]
        color = f'#FF{disagreement_color}{disagreement_color}'

        path_value = d["path"]
        if path_value == "":
            path_value = "root"

        worksheet.merge_range(path_row, 0, row - 1, 0, path_value,
                              workbook.add_format({
                                  'align': 'left',
                                  'valign': 'vcenter',
                                  'bg_color': color,
                                  'font_size': 10}
                              ))

        worksheet.merge_range(path_row, 1, row - 1, 1, "%.2f" % d["disagreement"], centered_text)


def get_ordered_BF_Info(order, info):
    results = []
    for o in order:
        i = next((i for i in info if i["significanceIndicator"] == o[0]), None)
        if i is not None:
            results += [i]

    return results


def write_specials_info(workbook, data, specials_file):
    info_order = [
        ("jet-jetbrains", "jet"),
        ("jet-pageRank", "jet-pg"),
        ("jet-degreeCentrality", "jet-dc"),
        ("jet-inDegreeCentrality", "jet-idc"),
        ("jet-outDegreeCentrality", "jet-odc"),
        ("jet-betweennessCentrality", "jet-bc"),
        ("ave-Avelino", "ave"),
        ("ave-pageRank", "ave-pg"),
        ("ave-degreeCentrality", "ave-dc"),
        ("ave-inDegreeCentrality", "ave-idc"),
        ("ave-outDegreeCentrality", "ave-odc"),
        ("ave-betweennessCentrality", "ave-bc")
    ]

    centered_text = workbook.add_format({
        'align': 'center',
        'valign': 'vcenter',
        'font_size': 10})

    left_text_format = workbook.add_format({
        'align': 'left',
        'valign': 'vcenter',
        'font_size': 10})

    header_format = workbook.add_format({
        'bold': 1,
        'align': 'center',
        'valign': 'vcenter',
        'fg_color': '000080',
        'font_color': 'white'})

    specials = [""] + np.loadtxt(specials_file, comments="#", dtype='str').tolist()
    specials_data = [d for d in data if d["path"] in specials]

    worksheet = workbook.add_worksheet("Specials")
    worksheet.set_column(0, 0, 40)
    worksheet.set_column(len(info_order) + 1, len(info_order) + 1, 30)

    worksheet.write(0, 0, 'root', header_format)
    worksheet.write(0, len(info_order) + 1, 'developers', header_format)
    worksheet.set_row(0, 40)

    for i, o in enumerate(info_order):
        worksheet.write(0, i+1, o[1], header_format)

    row = 1
    for d in specials_data:
        first_row = row
        info = get_ordered_BF_Info(info_order, d["info"])
        emails = list(set([d["email"] for i in info for d in i["developers"]]))
        if len(emails) > 0:
            for e in emails:
                worksheet.write(row, len(info) + 1, e, left_text_format)
                row += 1
        else:
            row += 1

        path = d["path"]
        if path == "":
            path = "root"

        write_merged(worksheet, first_row, row - 1, 0, path, left_text_format)
        for i, info in enumerate(info):
            write_merged(worksheet, first_row, row - 1, i + 1, info["busFactor"], centered_text)


if __name__ == "__main__":
    # probably missing logging here
    output_file, authorship_file, input_files, specials_file = get_args()
    inputs = load_inputs(input_files)
    authorship_info = load_authorship_info(authorship_file)
    data = merge_inputs(inputs)
    data = calculate_disagreement(data)

    workbook = xlsxwriter.Workbook(output_file)
    write_specials_info(workbook, data, specials_file)
    write_BF_Info(workbook, data)
    write_authorship_info(workbook, data, authorship_info)
    workbook.close()
