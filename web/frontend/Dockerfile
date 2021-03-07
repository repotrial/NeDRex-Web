FROM node:latest as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY ./ .

RUN npm run build

FROM andimajore/nedrex_repo:server_base as production-stage

WORKDIR /usr/app

RUN wget https://ftp-stud.hs-esslingen.de/pub/Mirrors/ftp.apache.org/dist/tomcat/tomcat-8/v8.5.63/bin/apache-tomcat-8.5.63.tar.gz
RUN tar -xzf apache-tomcat-8.5.63.tar.gz
RUN rm apache-tomcat-8.5.63.tar.gz
RUN mv apache-tomcat-8.5.63 apache-tomcat

WORKDIR apache-tomcat
RUN rm -rf webapps/*

COPY --from=build-stage /app/dist webapps/nedrex/
COPY docker/WEB-INF webapps/nedrex/WEB-INF

COPY docker/conf/* conf/

#EXPOSE 8080
#EXPOSE 8090

ENTRYPOINT bin/catalina.sh run
