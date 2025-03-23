#!/bin/sh
rm -r outdf/ *.jar bin/*
javac -classpath $(hadoop classpath):libs/opennlp-tools-1.9.3.jar -d bin questionfiveA.java
jar cmf META-INF/MANIFEST-A.MF df.jar -C bin/ . -C libs/ .
time hadoop jar df.jar data/Wikipedia-EN-20120601_ARTICLES/ outdf/ stopwords.txt
