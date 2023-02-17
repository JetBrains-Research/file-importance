import json
import os
import sys
import traceback

import pandas as pd
import requests
from pydriller import Repository

from utils import get_clusters


def log(log):
    print(f"****Developer Identifier**** {log}")


class UserInfo:
    @staticmethod
    def from_github_actor(actor):
        if actor is None:
            return None

        login = None
        if actor["user"] is not None:
            login = actor["user"]["login"]

        return UserInfo(actor["name"], actor["email"], login)

    def __init__(self, name, email, login=None):
        self.name = name
        self.email = email
        self.login = login


class UsersData:
    def __init__(self):
        self.users = set()
        self.email_count = {}
        self.name_count = {}

    def add_user(self, user):
        self.users.add(user)
        self.increment_count(self.name_count, user[0])
        self.increment_count(self.email_count, user[1])

    def add_users(self, users):
        for u in users:
            self.add_user(u)

    def increment_count(self, count_dictionary, name):
        if name not in count_dictionary:
            count_dictionary[name] = 0
        count_dictionary[name] += 1

    def create_dataframe(self):
        return pd.DataFrame({'email': [u[1] for u in self.users],
                             'name': [u[0] for u in self.users],
                             'login': [u[2] for u in self.users],
                             'initial_id': [f"{u[0]}:{u[1]}:{u[2]}" for u in self.users]})

    def get_email_count(self, email):
        if email not in self.email_count:
            return 0

        return self.email_count[email]

    def get_name_count(self, name):
        if name not in self.name_count:
            return 0

        return self.name_count[name]


class Args:
    def __init__(self, args):
        if len(args) < 8:
            print("PLease enter <Github Token> <Repository Owner> <Repository Name> "
                  "<Repository Local Path> <Avelino Output Path> <JetBrains Output path> <Users Output Path>")
            exit(1000)

        self.github_token = args[1]
        self.repository_owner = args[2]
        self.repository_name = args[3]
        self.repository_local_path = args[4]
        self.jetbrains_output_path = args[5]
        self.avelino_output_path = args[6]
        self.users_save_path = args[7]


def read_local_repository(path):
    log("Reading local Repository")
    local_repo = Repository(path, only_no_merge=True)
    local_info = {}
    for commit in local_repo.traverse_commits():
        local_info[commit.hash] = (commit.author, commit.committer)

    return local_info


def read_github_repository(github_token, repository_owner, repository_name):
    log("Reading github repo")

    github_info = {}
    try:
        headers = {
            "Authorization": ("Bearer %s" % github_token),
            "Accept": "application/vnd.github+json",
            "X-GitHub-Api-Version": "2022-11-28"
        }

        with open(f"{os.path.dirname(__file__)}/commitQueryTemplate.graphql") as f:
            query_template = f.read()

        query_template = query_template.replace("owner-placeholder", repository_owner)
        query_template = query_template.replace("name-placeholder", repository_name)

        cursor = None
        while True:
            if cursor is None:
                query = query_template.replace('after:"template"', '')
            else:
                query = query_template.replace('after:"template"', f'after:"{cursor}"')

            response = requests.post("https://api.github.com/graphql", headers=headers, json={"query": query})
            response_json = json.loads(response.text)
            commits = response_json["data"]["repository"]["defaultBranchRef"]["target"]["history"]["edges"]
            if len(commits) == 0:
                break

            for commit in commits:
                commit_data = commit["node"]
                author = UserInfo.from_github_actor(commit_data["author"])
                committer = UserInfo.from_github_actor(commit_data["committer"])
                github_info[commit_data["oid"]] = (author, committer)

            cursor = commits[-1]["cursor"]
            print(f"Next request cursor {cursor}")

    except Exception as e:
        print(e)
        print(traceback.format_exc())

    return github_info


def check_if_github_has_more_data(local, github):
    if github[0] is not None and github[1] is not None:
        return True

    if github[0] is None and github[1] is not None and local[1] != github[1]:
        return True

    if github[1] is None and github[0] is not None and local[0] != github[0]:
        return True

    return False


def merge_users_info(local, github):
    users = []
    if github:
        local = (local.name, local.email, github.login)
        users += [local]
        github = (github.name, github.email, github.login)
        if check_if_github_has_more_data(local, github):
            users += [github]
    else:
        users += [(local.name, local.email, None)]

    return users


def generate_jetbrains_output(clusters, path):
    out = []
    for c in clusters:
        emails = set()
        if len(clusters[c]) > 1:
            for d in clusters[c]:
                email = d.split(":")[1]
                if email != "None":
                    emails.add(email)
            emails = [(e, users_data.get_email_count(e)) for e in emails]
            sorted(emails, key=lambda ec: ec[1])
            emails = [ec[0] for ec in emails]
            out += [list(emails)]

    with open(path, "w") as f:
        json.dump(out, f)


def generate_avelino_output(clusters, path):
    out = ""
    for c in clusters:
        names = set([d.split(":")[0] for d in clusters[c]])
        names = [n for n in names if n != "None"]
        if len(names) > 1:
            names = [(n, users_data.get_name_count(n)) for n in names]
            sorted(names, key=lambda ec: ec[1])
            names = [nc[0] for nc in names]
            first = names[0]
            for n in names:
                out += f";{n};{first}\n"
        else:
            out += f";{names[0]};{names[0]}\n"

    with open(path, "w") as f:
        f.write(out)


def create_users_data(local_info, github_info):
    log("Creating users data")
    users_data = UsersData()
    for c in local_info:
        if c in github_info:
            local_author = local_info[c][0]
            local_committer = local_info[c][1]
            github_author = github_info[c][0]
            github_committer = github_info[c][1]

            users_data.add_users(merge_users_info(local_author, github_author))
            users_data.add_users(merge_users_info(local_committer, github_committer))
        else:
            local_author = local_info[c][0]
            local_committer = local_info[c][1]
            users_data.add_user((local_author.name, local_author.email, None))
            users_data.add_users((local_committer.name, local_committer.email, None))

    return users_data


def find_matches(users_data):
    users_df = users_data.create_dataframe()

    cluster_tags = get_clusters(users_df, 0.1)
    clusters = {}
    for user_info in cluster_tags:
        tag = cluster_tags[user_info]
        if tag not in clusters:
            clusters[tag] = []

        clusters[tag] += [user_info]

    return clusters


def save_clusters(clusters, path):
    with open(path, "w") as f:
        matches = [clusters[c] for c in clusters]
        json.dump(matches, f)


def print_clusters(clusters):
    print("****************************")
    for c in clusters:
        if len(clusters[c]) > 1:
            print(f"{clusters[c]}")

    print("Check for not matching usernames in clusters")
    for c in clusters:
        usernames = set([d.split(":")[-1] for d in clusters[c]])
        if len(usernames) > 1:
            print(clusters[c])


args = Args(sys.argv)

local_info = read_local_repository(args.repository_local_path)
github_info = read_github_repository(args.github_token, args.repository_owner, args.repository_name)
users_data = create_users_data(local_info, github_info)
clusters = find_matches(users_data)
generate_jetbrains_output(clusters, args.jetbrains_output_path)
generate_avelino_output(clusters, args.avelino_output_path)
save_clusters(clusters, args.users_save_path)
print_clusters(clusters)
