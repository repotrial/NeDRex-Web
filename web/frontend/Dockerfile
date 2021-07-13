FROM node:14.17 as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY ./ .
COPY docker/index.html public/index.html
COPY docker/vue.config.js ./vue.config.js
COPY docker/Config.js src/Config.js

RUN npm run build

FROM andimajore/nedrex_repo:server_base as production-stage
WORKDIR /usr/app

RUN wget https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.66/bin/apache-tomcat-8.5.66.tar.gz
RUN tar -xzf apache-tomcat-8.5.66.tar.gz
RUN rm apache-tomcat-8.5.66.tar.gz
RUN mv apache-tomcat-8.5.66 apache-tomcat
RUN rm -rf webapps/*

WORKDIR apache-tomcat
COPY --from=build-stage /app/dist webapps/ROOT/
COPY docker/WEB-INF webapps/ROOT/WEB-INF

COPY docker/conf/* conf/

#EXPOSE 8080
#EXPOSE 8090

#ENTRYPOINT tail -F conf/web.xml
ENTRYPOINT bin/catalina.sh run
