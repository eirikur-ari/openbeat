#!/bin/sh

CP=
for i in `ls openbeat/*.jar`
do
  CP=${CP}:${i}
done

for i in `ls lib/*.jar`
do
  CP=${CP}:${i}
done

java -Xmx512m -splash:openbeat/OpenBEATsplash.png -cp ".:${CP}" is.ru.openbeat.Launcher