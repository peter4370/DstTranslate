#!/bin/bash

LOCAL_PROPERTIES=.user.config

# check local properties
#echo "check if we have set steam library in $LOCAL_PROPERTIES"
if [ ! -f $LOCAL_PROPERTIES ]; then
    echo "Please enter your Steam library folder having DST installed."
    read -e -p "Steam Library: " -i "~/SteamLibrary" steam_library_path
    echo "export STEAM_LIBRARY=$steam_library_path" > $LOCAL_PROPERTIES
fi
. $LOCAL_PROPERTIES

# check installation
DST_DATA_BUNDLES="$STEAM_LIBRARY/steamapps/common/Don't Starve Together/data/databundles"
#ls -l $DST_DATA_BUNDLES
if [ ! -d "$DST_DATA_BUNDLES" ] || [ ! -f "$DST_DATA_BUNDLES/scripts.zip" ]; then
    echo "No DST installation found."
    exit 1
fi

# list old file info
ls -l android-app/app/src/main/assets/chinese_s.po

# unzip from dst scripts
unzip -p "$DST_DATA_BUNDLES/scripts.zip" scripts/languages/chinese_s.po > /tmp/chinese_s.po
ls -l /tmp/chinese_s.po

# copy as new file
cp /tmp/chinese_s.po android-app/app/src/main/assets/chinese_s.po
ls -l android-app/app/src/main/assets/chinese_s.po