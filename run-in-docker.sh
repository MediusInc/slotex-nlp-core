#!/bin/bash 
###################################################################
#Script Name   : run-in-docker.sh
#Description   : Run SloTex core project with this small script
#Args          :
#Author        : Tadej Justin
#Email         : tadej.justin@medius.si
###################################################################

set -e

check_if_var_is_set () {
    # check if dependant vars exits
    var="$1"
    if [[ -z ${!var} ]]; then
        echo "The $var is not set."
        exit 2
    fi
}

usage () {
    echo "Run SloTex docker-compose project."
    echo ""
    echo "Available options: "
    echo " --remove shout down running containers and remove all docker images from local docker registry"
    echo " --down shoot down running contaieners"
    echo " --check print docker status"
    echo " --env "

}

set_dependencies () {
    # set dependencies from exported images in docker-images directory
    # 1. set docker tags
    if [[ -d docker-images ]]; then
        for f in docker-images/*gz; do 
            iout="$(gunzip -c "$f" | docker load)"
            image=$(echo "$iout" | grep "Loaded image" | cut -d ":" -f 2-)
            if echo "$image" | grep -q "redis"; then
                REDIS_VERSION=$(echo "$image" | rev | cut -d ":" -f 1 | rev)
            elif echo "$image" | grep -q "mongo"; then
                MONGO_VERSION=$(echo "$image" | rev | cut -d ":" -f 1 | rev)
            elif echo "$image" | grep -q "medius-nlp"; then
                SLOTEX_NLP_VERSION=$(echo "$image" | rev | cut -d ":" -f 1 | rev)
            fi
        done
    else
        echo "There are is no 'docker-images' direcoty in $(pwd)".
        exit 1
    fi

    echo "export REDIS_TAG=${REDIS_VERSION}" > dependencies
    { echo "export MONGO_TAG=${MONGO_VERSION}" 
      echo "export SLOTEX_NLP_TAG=${SLOTEX_NLP_VERSION}" 
    } >> dependencies
}


## 1. set docker tags
if [ ! -f dependencies ]; then
    set_dependencies
fi
source dependencies

check_if_var_is_set REDIS_TAG
check_if_var_is_set MONGO_TAG
check_if_var_is_set SLOTEX_NLP_TAG

# parse args and switches
options=$(getopt -o hrpdce: --long help,remove,pull,down,check,env: -n 'parse-options' -- "$@")
if [ "$?" != "0" ]; then  
    echo "ERROR options provided"
    usage;
    exit 1
fi
eval set -- "$options"

run=true
pull=false
down=false
check=false
rmi=false
env=false
while true; do
    case "$1" in
    -h | --help ) usage; shift; exit;;
    -p | --pull ) pull=true; shift; shift ;;
    -d | --down ) down=true; shift; shift ;;
    -c | --check ) check=true; shift; shift ;;
    -r | --remove ) rmi=true; shift; shift ;;
    -e | --env ) env="$2"; shift; shift ;;
    -- ) shift; break;;
    *) usage; break;;
    esac
done

if [ "$env" = false ]; then
    echo "Please set --env argument"
    usage
    exit 2
fi

if $pull; then
    if [ "$env" != "it-test" ]; then
        docker-compose pull -q
    else
        docker-compose pull -q redis mongodb
    fi
    run=false
fi

if $down; then
    if [ "$env" != "it-test" ]; then
        docker-compose down -v
    else
        docker-compose down -v redis mongodb
    fi
    run=false
fi

if $rmi; then
    if [ "$env" != "it-test" ]; then
        docker-compose down -v --rmi all
    else
        docker-compose down -v --rmi redis mongodb
    fi
    run=false
fi

if $check; then
    docker-compose -f docker-compose.yml config -q
    run=false
fi

if $run; then
    if [ "$env" != "it-test" ]; then
        docker-compose up -d
    else
        docker-compose up -d redis mongodb
    fi
fi

exit 0
