#!/bin/bash
v="$(<module.template)"
p="$(<pom.template)"
NL=$'\n'

modlist=""

while read version; do


  modlist="$modlist        <module>$version</module>$NL"
  mkdir -p "$version"
  echo "${v/<<VERSION>>/$version}" >  $version/pom.xml

done < versions.txt

  echo "${p/<<MODULES>>/$modlist}" > pom.xml
