#!/usr/bin/env bash


cp frontend/docker/configs/Config_$1.js frontend/docker/Config.js
cp frontend/docker/configs/vue.config_$1.js frontend/docker/vue.config.js
cp compose-scripts/docker-compose_$1.yml ./docker-compose.yml
docker-compose build
#rm docker-compose.yml

cp frontend/docker/configs/Config_prod.js frontend/docker/Config.js
cp frontend/docker/configs/vue.config_prod.js frontend/docker/vue.config.js
