import json
import pandas as pd


class ElementInformation:
    def __init__(self, element_name, file_name):
        self.element_name = element_name
        self.file_name = file_name


class ExecutionInformation:
    def __init__(self, element_information, time_ms, average_time_ms, own_time_ms, count):
        self.element_information = element_information
        self.own_time_ms = own_time_ms
        self.average_time_ms = average_time_ms
        self.time_ms = time_ms
        self.count = count


def parse_information(info_file_path, execution_file_path):
    element_infos = get_element_information(info_file_path)
    result = get_execution_information(element_infos, execution_file_path)
    return result


def get_execution_information(element_infos, execution_file_path):
    result = []
    with open(execution_file_path) as f:
        df = pd.read_csv(f)
        for _, row in df.iterrows():
            try:
                method_name, file_name = row[0].split(' ')
                class_name = method_name[:method_name.rfind('.', 0, method_name.find('('))]
                element_info = next((ei for ei in element_infos if ei.element_name == class_name), None)
                if element_info is not None:
                    result += ExecutionInformation(element_info, parse_number(row[1]), parse_number(row[2]), parse_number(row[3]), parse_number(row[4]))
                else:
                    print(f"Can not find {class_name}")
            except Exception as error:
                pass
                # print(f"Can not pars {row}")
                # print(f"error: {error}")

    return result


def parse_number(number):
    if '<' in number:
        number = '0'

    return int(number, 16)


def get_element_information(info_file_path):
    element_infos = []
    with open(info_file_path) as f:
        jsonData = json.load(f)
        for jsonInfo in jsonData:
            element_infos += [ElementInformation(jsonInfo['elementName'], jsonInfo['fileName'])]
    return element_infos


result = parse_information('../shared/info.json', '../shared/Method-list--CPU.csv')
print(json.dumps(result))