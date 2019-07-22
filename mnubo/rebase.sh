#!/bin/bash -ex

# This script should only be executed on `master`

# Add the remote, get the latest code and rebase our changes on top
git remote rm upstream || echo "Upstream does not exist already"
git remote add upstream https://github.com/fthomas/scala-steward.git
git fetch upstream
git rebase upstream/master

# Force is required as we rewrite history
git checkout -B chore/rebasing
git push --force "gitlab@git-lab1.mtl.mnubo.com:$CI_PROJECT_PATH.git" HEAD:master --push-option=ci.skip

git tag "$VERSION"
git push "gitlab@git-lab1.mtl.mnubo.com:$CI_PROJECT_PATH.git" "$VERSION"
