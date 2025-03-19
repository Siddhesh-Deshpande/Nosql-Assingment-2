#!/bin/bash
rm -r *.class *.jar ../outputs/output_partA/
javac -classpath $(hadoop classpath)  ../src/PartA.java -d .
jar -cvf PartA.jar *.class
time hadoop jar PartA.jar PartA ../stopwords.txt ../data/Wikipedia-EN-20120601_ARTICLES ../outputs/output_partA