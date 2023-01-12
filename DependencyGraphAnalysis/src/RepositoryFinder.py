from github import Github


def print_repositories(repositories):
    count = 0
    for repo in repositories:
        languages = repo.get_languages()
        print(f"{repo.name} : {repo.stargazers_count} *** languages: {languages}")
        count += 1

        if count > 5:
            break


g = Github()

repositories = g.search_repositories(query='Java', sort='stars', order='desc')
print_repositories(repositories)
repositories = g.search_repositories(query='Kotlin', sort='stars', order='desc')
print_repositories(repositories)