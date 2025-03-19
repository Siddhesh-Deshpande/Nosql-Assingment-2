#!/bin/bash
rm -r *.class *.jar ../outputs/output_FunctionLevelStripes/
javac -classpath $(hadoop classpath)  ../src/FunctionLevelStripes.java -d .
jar -cvf FunctionLevelStripes.jar *.class
time hadoop jar FunctionLevelStripes.jar FunctionLevelStripes ../outputs/output_partA/part-r-00000 ../data/Wikipedia-EN-20120601_ARTICLES ../outputs/output_FunctionLevelStripes
