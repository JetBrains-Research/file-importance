import xlsxwriter

from github import Github


def process_repository(repositories):
    for repo in repositories:
        languages = repo.get_languages()
        total_language = 0
        jk_language = 0
        for l in languages:
            if l == 'Java' or l == 'Kotlin':
                jk_language += languages[l]

            total_language += languages[l]

        coverage = 0
        if total_language > 0:
            coverage = jk_language / total_language

        repo.lang_coverage = coverage
        repo.lang = languages


def print_repositories(repositories):
    for repo in repositories:
        languages = repo.get_languages()
        print(f"{repo.name} : {repo.stargazers_count} *** {repo.lang_coverage} -> {languages}")


def export_results(repositories):
    workbook = xlsxwriter.Workbook("test.xlsx")
    worksheet = workbook.add_worksheet()

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

    worksheet.set_row(0, 40)
    worksheet.set_column(0, 0, 5)
    worksheet.set_column(1, 1, 50)
    worksheet.set_column(2, 2, 50)
    for i in range(3, 7):
        worksheet.set_column(i, i, 25)

    worksheet.write(0, 0, "Number", header_format)
    worksheet.write(0, 1, "Repository", header_format)
    worksheet.write(0, 2, "Base Url", header_format)
    worksheet.write(0, 3, "Stars", header_format)
    worksheet.write(0, 4, "Java Kotlin Percentage", header_format)
    worksheet.write(0, 5, "Language Name", header_format)
    worksheet.write(0, 6, "File Size", header_format)

    row = 1
    count = 0
    for repo in repositories:
        count += 1
        first_row = row
        for l in repo.lang:
            worksheet.write(row, 5, l, centered_text)
            worksheet.write(row, 6, repo.lang[l], centered_text)
            row = row + 1

        if row == first_row:
            row = row + 1

        if row - first_row > 1:
            worksheet.merge_range(first_row, 0, row - 1, 0, count, centered_text)
            worksheet.merge_range(first_row, 1, row - 1, 1, repo.full_name, left_text_format)
            worksheet.merge_range(first_row, 2, row - 1, 2, repo.html_url, left_text_format)
            worksheet.merge_range(first_row, 3, row - 1, 3, repo.stargazers_count, centered_text)
            worksheet.merge_range(first_row, 4, row - 1, 4, "%.2f" % repo.lang_coverage, centered_text)
        else:
            worksheet.write(first_row, 0, count, centered_text)
            worksheet.write(first_row, 1, repo.full_name, left_text_format)
            worksheet.write(first_row, 2, repo.html_url, left_text_format)
            worksheet.write(first_row, 3, repo.stargazers_count, centered_text)
            worksheet.write(first_row, 4, "%.2f" % repo.lang_coverage, centered_text)

    workbook.close()


def get_high_stars(it, min_start_cunt):
    result = []
    for i in it:
        if i.stargazers_count < min_start_cunt:
            break

        result += [i]
    return result


if __name__ == "__main__":
    g = Github(login_or_token="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")
    queries = ["Java", "Kotlin"]
    all_repositories = []
    for q in queries:
        fetched_repositories = g.search_repositories(query=q, sort='stars', order='desc')
        repositories = get_high_stars(fetched_repositories, 10000)
        process_repository(repositories)
        all_repositories += repositories

    all_repositories = sorted(all_repositories, key=lambda r: r.stargazers_count, reverse=True)
    print("Exporting results")
    # print_repositories(all_repositories)
    export_results(all_repositories)
