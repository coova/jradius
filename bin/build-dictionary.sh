#!/bin/sh
dir=`pwd`; cd `dirname $0`; bin=./;

dictdir=freeradius/dict
srcdir=build/dictionary-src 
bindir=build/dictionary

[ -e "$dictdir" ] || unzip freeradius.zip
[ -e "$srcdir" ] || mkdir -p $srcdir
[ -e "$bindir" ] || mkdir -p $bindir

classpath="$CLASSPATH:$bin/jradius.jar:$bin/jradius-dictionary.jar"
CLASSPATH="$classpath" java net.sf.jradius.freeradius.RadiusDictionary \
  net.sf.jradius.dictionary $dictdir $srcdir

CLASSPATH="$classpath" javac -source 1.4 -target 1.4 -d $bindir `find $srcdir|grep .java`

(cd $bindir; jar cvf $dir/jradius-dictionary.jar *)
