#!/bin/sh
(cd `dirname $0`; classpath=".:./lib"
for jar in ./lib/*.jar; do
  classpath="$classpath:$jar" 
done
CLASSPATH="$classpath" java net.jradius.client.gui.JRadiusSimulator)

