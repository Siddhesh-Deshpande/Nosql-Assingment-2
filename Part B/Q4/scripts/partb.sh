#!/bin/bash
rm -r *.class *.jar ../outputs/output_partB/
javac -classpath $(hadoop classpath)  ../src/PartB.java -d .
jar -cvf PartB.jar *.class
time hadoop jar PartB.jar PartB ../outputs/output_partA/part-r-00000 ../data/Wikipedia-EN-20120601_ARTICLES ../outputs/output_partB