#!/bin/sh
dir=`pwd`; cd `dirname $0`; bin=`pwd`; cd $dir
classpath="$CLASSPATH:$bin/jradius.jar:$bin/jradius-dictionary.jar"
for jar in $bin/lib/*.jar; do
  classpath="$classpath:$jar" 
done
classpath="$classpath:$bin/lib/" 
CLASSPATH="$classpath" java net.jradius.client.gui.JRadiusSimulator $*
