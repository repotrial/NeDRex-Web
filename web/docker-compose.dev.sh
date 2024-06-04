#!/usr/bin/env bash

#cd backend
#mvn clean package -Dmaven.test.skip
#cd ..
#cp frontend/docker/configs/Config_dev.js frontend/docker/Config.js
#cp frontend/docker/configs/vue.config_dev.js frontend/docker/vue.config.js
cp compose-scripts/docker-compose_dev.yml ./docker-compose.yml
#cp frontend/docker/configs/index_dev.html frontend/docker/index.html
docker compose build
rm docker-compose.yml

cp frontend/docker/configs/Config_prod.js frontend/docker/Config.js
cp frontend/docker/configs/vue.config_prod.js frontend/docker/vue.config.js
cp frontend/docker/configs/index_prod.html frontend/docker/index.html
cp compose-scripts/docker-compose_prod.yml ./docker-compose.yml

#docker tag andimajore/nedrex_repo:server_prod andimajore/nedrex_repo:server_dev

#docker push andimajore/nedrex_repo:server_dev
#docker push andimajore/nedrex_repo:web_prod
