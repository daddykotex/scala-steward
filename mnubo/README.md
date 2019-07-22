# Mnubo Scala Steward

scala-steward is a great tool to help you automate your dependencies upgrade. It officially
lives here: https://github.com/fthomas/scala-steward.

## Build

Use sbt build the docker image by running: `sbt docker:publishLocal`.

## Development

The development of scala-steward happens upstream. This repository is a mirror of the official
one, with a few extra commits to allow us to run it in here. There is a manual GitLab CI pipeline
that allows us to keep up with the changes. See the `.gitlab-ci.yml`.

List of changes we made on top of the original source code:

- Anything under `mnubo/`
- A change to the PR title format
- A change to set the PR to be closed on merge

### Pulling changes

Once upon a time, it's recommended that you run the `project:rebase` pipeline in Gitlab.
This will launch `./mnubo/rebase.sh` which will fetch the latest changes from GitHub and
rebase our changes on top of it.

This pipeline is manual so it must be triggered from the Gitlab UI. It will also push a
new tag which will generate a new image and push it to our docker registry.

## Running

Coming soon in K8S.
