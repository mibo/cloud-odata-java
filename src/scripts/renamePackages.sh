#!/bin/bash

OLD=com\.sap\.core
NEW=de\.mirb\.od

ENDINGS=(java xml properties)

for ending in "${ENDINGS[@]}"
do
  echo Search for $OLD replace with $NEW in files with ending $ending
  find ./ -type f -iname "*$ending" | xargs grep "$OLD" -l | xargs sed -e "s/$OLD/$NEW/g" -i ''
done

