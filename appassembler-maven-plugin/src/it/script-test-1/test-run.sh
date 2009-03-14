#!/bin/sh

set -x
dirname=`dirname $0`
hello=$dirname/target/generated-resources/appassembler/unix/bin/hello-world

chmod +x $hello

REPO=$dirname/target/appassembler/repo
export REPO
exec sh -x $hello $1
