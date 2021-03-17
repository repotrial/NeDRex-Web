FROM andimajore/nedrex_repo:server_base as production-stage

ENV PYTHONPATH=$PYTHONPATH:/usr/local/lib/python3.8/dist-packages/
ENV PYTHON=/usr/bin/python3
ENV JAVA_HOME=/usr/app/jdk-15.0.1
ENV PATH=$PATH:$JAVA_HOME/bin

RUN mkdir scripts cache
WORKDIR /usr/app/scripts
COPY scripts/* ./

RUN pipreqs ./
RUN cat requirements.txt | grep -v "graph_tool" > reqs.txt
RUN pip3 install -r reqs.txt
RUN pip3 uninstall -y networkx
RUN pip3 install networkx==2.3

WORKDIR /usr/app

#RUN wget https://ftp-stud.hs-esslingen.de/pub/Mirrors/ftp.apache.org/dist/tomcat/tomcat-8/v8.5.64/bin/apache-tomcat-8.5.64.tar.gz
#RUN tar -xzf apache-tomcat-8.5.64.tar.gz
#RUN rm apache-tomcat-8.5.64.tar.gz
#RUN mv apache-tomcat-8.5.64 apache-tomcat
#RUN rm -rf apache-tomcat/webapps/*

COPY config/ config/
COPY src/main/resources/application-prod.properties config/server/
RUN mkdir cache

#WORKDIR apache-tomcat
#COPY docker/conf/* conf/
#RUN mkdir -p webapps/nedrex_backend
#WORKDIR webapps/nedrex_backend
COPY target/nedrexweb-backend.war ./
#RUN unzip nedrexweb-backend.war
#RUN rm nedrexweb-backend.war

#WORKDIR /usr/app
COPY scripts/wait-for-it.sh wait-for-it.sh

#ENTRYPOINT ./wait-for-it.sh nedrex-web-db:3306 -t 30 -- apache-tomcat/bin/catalina.sh run Dspring.profiles.active=prod
ENTRYPOINT ./wait-for-it.sh nedrex-web-db:3306 -t 30 -- java -Xmx4g -jar nedrexweb-backend.war
