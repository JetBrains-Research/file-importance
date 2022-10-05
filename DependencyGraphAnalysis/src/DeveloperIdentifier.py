import sys
import gambit
import pandas as pd


class DeveloperInfo:
    def __init__(self, name, email):
        self.name = name
        self.email = email


def log(log):
    print(f"****Developer Identifier**** {log}")


def read_all_developers(path):
    results = []
    with open(path) as file:
        for line in file:
            splits = line.split(";")
            if len(splits) == 2:
                results += [DeveloperInfo(splits[0], splits[1])]
            else:
                log(f"Line should contains only one ; as seperator. Can not parse {line}")

    return results


def pars_args(args):
    all_developers_Path = args[1]
    if all_developers_Path is None:
        log("Please enter all developers files path")
        exit(1120)
    alias_file_path = args[2]
    if alias_file_path is None:
        log("Please enter alias file path")
        exit(1121)

    return all_developers_Path, alias_file_path


(all_developers_Path, alias_file_path) = pars_args(sys.argv)

all_developers = read_all_developers(all_developers_Path)
all_names = [d.name for d in all_developers]
all_emails = [d.email for d in all_developers]
data = pd.DataFrame({
    "alias_name": all_names,
    "alias_email": all_emails
})

authors = gambit.disambiguate_aliases(data)
aliases = [None] * (authors.author_id.max() + 1)
for i in range(len(authors.author_id)):
    if aliases[authors.author_id[i]] is None:
        aliases[authors.author_id[i]] = authors.alias_name[i]

results = []
for i in range(len(authors.author_id)):
    results += [(authors.alias_name[i], aliases[authors.author_id[i]])]

results = set(results)
results = list(results)
results.sort(key=lambda x: x[1])

with open(alias_file_path, "w") as file:
    newLine = False
    for r in results:
        if newLine:
            file.write("\n")
        else:
            newLine = True

        file.write(f";{r[0]};{r[1]}")


