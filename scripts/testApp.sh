#!/bin/bash
set -euxo pipefail

##############################################################################
##
##  GH actions CI test script
##
##############################################################################

mvn -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -ntp -pl system -q clean package liberty:create liberty:install-feature liberty:deploy
mvn -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -ntp -pl frontend -q clean package liberty:create liberty:install-feature liberty:deploy

mvn -ntp -pl system liberty:start
mvn -ntp -pl frontend liberty:start

mvn -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -ntp -pl system failsafe:integration-test

sleep 10
grep loadAverage frontend/target/liberty/wlp/usr/servers/defaultServer/logs/messages.log || exit 1
grep memoryUsage frontend/target/liberty/wlp/usr/servers/defaultServer/logs/messages.log || exit 1

mvn -ntp -pl system liberty:stop
mvn -ntp -pl frontend liberty:stop

mvn -ntp failsafe:verify
