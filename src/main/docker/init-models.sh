#!/bin/bash 
###################################################################
#Script Name   : init-models.sh
#Description   : Init models to binded mount
#Args          : 
#Author        : Tadej Justin                                                
#Email         : tadej.justin@medius.si
###################################################################
set -e

check_and_init () {
if [ ! -d "/var/data/$1" ]; then
    mkdir "/var/data/$1"
    printf "%s\\n" "The $1 directory does not exist: /var/data/$1"
    printf "%s\\n" "Copy init structure:"
    cp -r "/data/$1" /var/data/
    printf "%s%s\\n" "Initial $1 copeid on:" "$(date)" > "/var/data/$1/.initialized"
elif [ -f "/var/data/$1/.initialized" ]; then
    content=$(cat "/var/data/$1/.initialized")
    printf "%s\\n%s" "Initialization done at: " "$content"
else 
    printf "%s\\n" "Unknow error when initializing models"
    exit 2
fi
}

declare -a dir_structure=(dict lang lemma models ner pos stc token)

for s in ${dir_structure[*]}; do
    check_and_init "$s"
done

mkdir -p /var/data/models/backup

exit 0
