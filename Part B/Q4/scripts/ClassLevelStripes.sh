#!/bin/bash
rm -r *.class *.jar ../outputs/output_part_ClassLevelStripes/
javac -classpath $(hadoop classpath)  ../src/ClassLevelStripes.java -d .
jar -cvf ClassLevelStripes.jar *.class
time hadoop jar ClassLevelStripes.jar ClassLevelStripes ../outputs/output_partA/part-r-00000 ../data/Wikipedia-EN-20120601_ARTICLES ../outputs/output_part_ClassLevelStripes
