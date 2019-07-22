#!/bin/bash -e

sbt ";project core; set version := \"$CI_COMMIT_TAG\"; set Docker / packageName := \"dockerep-0.mtl.mnubo.com/scala-steward\"; docker:publishLocal"
docker tag "dockerep-0.mtl.mnubo.com/scala-steward:$CI_COMMIT_TAG" dockerep-0.mtl.mnubo.com/scala-steward:latest
docker push dockerep-0.mtl.mnubo.com/scala-steward:latest
docker push "dockerep-0.mtl.mnubo.com/scala-steward:$CI_COMMIT_TAG"
