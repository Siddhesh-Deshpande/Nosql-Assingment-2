#!/bin/sh
rm -r outtfidf/ *.jar bin/*
javac -classpath $(hadoop classpath):libs/opennlp-tools-1.9.3.jar -d bin questionfiveB.java
jar cmf META-INF/MANIFEST-B.MF tfidf.jar -C bin/ . -C libs/ .
time hadoop jar tfidf.jar data/Wikipedia-EN-20120601_ARTICLES/ outtfidf/ ./outdf/part-r-00000
