#!/bin/sh

REPO=target/appassembler/repo
export REPO
exec target/generated-resources/appassembler/booter-unix/bin/hello-world %1
