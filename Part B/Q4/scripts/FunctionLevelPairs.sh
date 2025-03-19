#!/bin/bash
rm -r *.class *.jar ../outputs/output_part_FunctionLevelPairs/
javac -classpath $(hadoop classpath)  ../src/FunctionLevelPairs.java -d .
jar -cvf FunctionLevelPairs.jar *.class
time hadoop jar FunctionLevelPairs.jar FunctionLevelPairs ../outputs/output_partA/part-r-00000 ../data/Wikipedia-EN-20120601_ARTICLES ../outputs/output_part_FunctionLevelPairs