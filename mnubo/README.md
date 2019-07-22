# Mnubo Scala Steward

scala-steward is a great tool to help you automate your dependencies upgrade. It officially
lives here: https://github.com/fthomas/scala-steward.

## Build

Use sbt build the docker image by running: `sbt docker:publishLocal`.

## Development

The development of scala-steward happens upstream. This repository is a mirror of the official
one, with a few extra commits to allow us to run it in here. There is a manual GitLab CI pipeline
that allows us to keep up with the changes. See the `.gitlab-ci.yml`.

## Running

When you've built the docker image, take note of that image name and use it in the
following command:

```bash
docker run -it \
 --rm \
 -v "sbt:/root/.sbt" \
 -v "ivy2:/root/.ivy2" \
 -v /Users/davidfrancoeur/workspace/scala-steward/tmprunning:/data \
 fthomas/scala-steward:0.3.0-131-47e40fda \
 -d \
 -DROOT_LOG_LEVEL="DEBUG" \
 --workspace "/data/workspace/" \
 --repos-file "/data/repos.md" \
 --git-author-name "Scala Steward" \
 --git-author-email "dfrancoeur+steward@mnubo.com" \
 --vcs-type "gitlab" \
 --vcs-api-host "https://git-lab1.mtl.mnubo.com/api/v4/" \
 --vcs-login "scala-steward" \
 --do-not-fork \
 --git-ask-pass "/data/pass.sh"
```
