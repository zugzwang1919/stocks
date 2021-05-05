#!/bin/bash
nohup java -Xms512m -Xmx758m -jar -Dspring.profiles.active=prod $1 > /dev/null 2>&1 &
