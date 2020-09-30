# Introduction
Currently, the Scala Steward creates a pull-request with a specific branch pattern. I hope to reuse this branch pattern to find opened PR on branches that matches this pattern. 

Suppose the bot is working with `org.typelevel:cats`. If there is an opened PR on the project to update from `2.0.0` to `2.0.1` (branch: `update/cats-2.0.1`) and when the bot runs again, it opens a new PR to update from `2.0.0` to `2.0.2` (branch: `update/cats-2.0.2`), we'd like the bot to close the older PR.

This is to prevent PRs to pile up.

# Details

The pattern looks like this: `update/${artifact}-${version}`. If the various providers give us a way to get only opened branches that match the first part of the pattern: `update/${artifact}` (i.e.: ignoring the version)

So the bot needs to:

1. list branches in the repository (locally using `git`)
2. filter out the current branch, and keep only branches related to the current main artifact id we're updating
3. foreach branch, check whether a pull request is open for it, if yes, close it


# References:

Github Pulls API: https://docs.github.com/en/free-pro-team@latest/rest/reference/pulls

Gitlab MRs API: https://docs.gitlab.com/ee/api/merge_requests.html#list-merge-requests

Bitbucket pullrequests API https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Bworkspace%7D/%7Brepo_slug%7D/pullrequests