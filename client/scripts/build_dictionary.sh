#!/bin/sh
cd `dirname $0`
pwd=$(pwd)
classpath=".:$pwd/lib"

# pickup jradius-core into classpath
for jar in $pwd/lib/*jradius-core*.jar; do
  classpath="$classpath:$jar" 
done

# generate source
CLASSPATH="$classpath" java net.jradius.freeradius.RadiusDictionary net.jradius.dictionary share tmp-dictionary

# compile and jar
(cd tmp-dictionary
 find . -name \*.java -print > file.list
 echo "Compiling $(wc -l file.list) classes"
 CLASSPATH="$classpath" javac @file.list
 echo "Creating jar jradius-dictionary.jar"
 find . -name \*.class -print > class.list
 jar cf ../jradius-dictionary.jar @class.list
)
