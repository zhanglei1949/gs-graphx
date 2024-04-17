#!/bin/bash

set -eo pipefail

GLT_ROOT_DIR=$(dirname $(dirname "$(realpath "$0")"))

echo "Running cpp unit tests ..."

cd $GLT_ROOT_DIR/built/bin/

for i in `ls test_*`
  do ./$i
done
