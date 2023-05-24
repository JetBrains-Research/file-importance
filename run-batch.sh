#!/usr/bin/env bash

if [ $# -ne "1" ]; then
  echo "usage: run-batch <github token>"
  exit 1
fi

githubToken=$1
repositories="repositories"

# repos=( "square/retrofit")
readarray -t repos < "./repos.txt"

for i in "${repos[@]}"
do
   : 
   IFS='/' read -r -a repo <<< "$i";
   repoOwner=${repo[0]}
   repoName=${repo[1]}
   "./prepare.sh" "$repoOwner" "$repoName" "$githubToken" && "./run.sh" "$repositories/$repoName"
done