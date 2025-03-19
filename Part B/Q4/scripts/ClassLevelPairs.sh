#!/bin/bash
rm -r *.class *.jar ../outputs/output_part_ClassLevelPairs/
javac -classpath $(hadoop classpath)  ../src/ClassLevelPairs.java -d .
jar -cvf ClassLevelPairs.jar *.class
time hadoop jar ClassLevelPairs.jar ClassLevelPairs ../outputs/output_partA/part-r-00000 ../data/Wikipedia-EN-20120601_ARTICLES ../outputs/output_part_ClassLevelPairs
