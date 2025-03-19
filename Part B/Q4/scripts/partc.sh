#!/bin/bash
rm -r *.class *.jar ../outputs/output_partC/
javac -classpath $(hadoop classpath)  ../src/PartC.java -d .
jar -cvf PartC.jar *.class
time hadoop jar PartC.jar PartC ../outputs/output_partA/part-r-00000 ../data/Wikipedia-EN-20120601_ARTICLES ../outputs/output_partC