FROM node:14.17 as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY ./ .
COPY docker/index.html public/index.html
COPY docker/vue.config.js ./vue.config.js
COPY docker/Config.js src/Config.js

RUN npm run build

FROM ubuntu:latest as production-stage
WORKDIR /usr/app

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y wget

RUN wget https://download.java.net/java/GA/jdk20.0.2/6e380f22cbe7469fa75fb448bd903d8e/9/GPL/openjdk-20.0.2_linux-x64_bin.tar.gz
RUN tar -xzf openjdk-20.0.2_linux-x64_bin.tar.gz
RUN rm openjdk-20.0.2_linux-x64_bin.tar.gz
ENV JAVA_HOME=/usr/app/jdk-20.0.2
ENV PATH=$PATH:$JAVA_HOME/bin

RUN wget https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.66/bin/apache-tomcat-8.5.66.tar.gz
RUN tar -xzf apache-tomcat-8.5.66.tar.gz
RUN rm apache-tomcat-8.5.66.tar.gz
RUN mv apache-tomcat-8.5.66 apache-tomcat
RUN rm -rf webapps/*

RUN apt-get remove -y wget xz-utils perl

WORKDIR apache-tomcat
COPY --from=build-stage /app/dist webapps/ROOT/
COPY docker/WEB-INF webapps/ROOT/WEB-INF

COPY docker/conf/* conf/

#EXPOSE 8080
#EXPOSE 8090

#ENTRYPOINT tail -F conf/web.xml
ENTRYPOINT bin/catalina.sh run
