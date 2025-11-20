#!/bin/bash
set -euxo pipefail
./mvnw -version

##############################################################################
##
##  GH actions CI test script
##
##############################################################################

./mvnw -ntp -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -ntp -pl system -q clean package liberty:create liberty:install-feature liberty:deploy
./mvnw -ntp -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -ntp -pl client -q clean package liberty:create liberty:install-feature liberty:deploy

./mvnw -ntp -pl system liberty:start
./mvnw -ntp -pl client liberty:start

./mvnw -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -ntp -pl system failsafe:integration-test

sleep 20
grep cpuLoad client/target/liberty/wlp/usr/servers/defaultServer/logs/messages.log || \
sleep 20 || \
grep cpuLoad client/target/liberty/wlp/usr/servers/defaultServer/logs/messages.log || exit 1
grep memoryUsage client/target/liberty/wlp/usr/servers/defaultServer/logs/messages.log || \
sleep 20 || \
grep memoryUsage client/target/liberty/wlp/usr/servers/defaultServer/logs/messages.log || exit 1

./mvnw -ntp -pl system liberty:stop
./mvnw -ntp -pl client liberty:stop

./mvnw -ntp failsafe:verify
