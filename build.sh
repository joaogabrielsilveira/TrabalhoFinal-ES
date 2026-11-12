#!/bin/bash
set -e
rm -rf out && mkdir -p out
javac -cp "lib/*" -d out *.java
java -cp "out:lib/*" Main
