#!/bin/bash 
###################################################################
#Script Name   : init-models.sh
#Description   : Init models to binded mount
#Args          : 
#Author        : Tadej Justin                                                
#Email         : tadej.justin@medius.si
###################################################################
set -e

if [ ! -d /var/data/models ]; then
    mkdir /var/data/models
    printf "%s\\n" "The modles directory does not exist: /var/data/models"
    printf "%s\\n" "Copy init models:"
    cp -r /opt/data/models /var/data/
    printf "%s%s\\n" "Initial models copeid on:" "$(date)" > /var/data/models/.initialized
elif [ ! -d /var/data/models/backup ]; then
    printf "%s\\n" "Error the /var/data/models/backup direcotry does not exist."
    exit 1
elif [ -f /var/data/models/.initialized ]; then
    content=$(cat /var/data/models/.initialized)
    printf "%s\\n%s" "Initialization done at: " "$content"
else 
    printf "%s\\n" "Unknow error when initializing models"
    exit 2
fi

exit 0
