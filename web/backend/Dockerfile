FROM andimajore/nedrex_repo:server_base

ENV PYTHONPATH=$PYTHONPATH:/usr/local/lib/python3.8/dist-packages/
ENV PYTHON=/usr/bin/python3
ENV JAVA_HOME=/usr/app/jdk-15.0.1
ENV PATH=$PATH:$JAVA_HOME/bin

RUN mkdir scripts cache
WORKDIR /usr/app/scripts
COPY scripts* .

RUN pipreqs ./
RUN cat requirements.txt | grep -v "graph_tool" > reqs.txt
RUN pip3 install -r reqs.txt
RUN pip3 uninstall -y networkx
RUN pip3 install networkx==2.3

WORKDIR /usr/app

COPY scripts/wait-for-it.sh wait-for-it.sh

RUN wget https://ftp-stud.hs-esslingen.de/pub/Mirrors/ftp.apache.org/dist/tomcat/tomcat-8/v8.5.63/bin/apache-tomcat-8.5.63.tar.gz
RUN tar -xzf apache-tomcat-8.5.63.tar.gz
RUN rm apache-tomcat-8.5.63.tar.gz
RUN mv apache-tomcat-8.5.63 apache-tomcat

COPY config/db_config.json config/db/
COPY src/main/resources/application-prod.properties config/server/
RUN mkdir cache

WORKDIR apache-tomcat

COPY target/nedrexweb-backend.war ./
RUN mkdir nedrexweb-backend
WORKDIR nedrexweb-backend
RUN unzip ../nedrexweb-backend.war
WORKDIR ../
RUN rm nedrexweb-backend.war

#RUN apt update
#RUN apt install less
RUN mkdir conf/Catalina conf/Catalina/localhost
RUN echo '<Context path="/backend" docBase="../nedrexweb-backend"/>' > conf/Catalina/localhost/nedrexweb-backend.xml
RUN echo 'spring.profiles.active=prod' >> conf/catalina.properties

COPY scripts/tomcatenv.sh bin/setenv.sh

RUN rm -rf webapps/*


#adjust server.xml
#copy script to set context path env var
#copy script to set port
#hosturl necessary?

#EXPOSE 8090

#CMD "tail -f scripts/wait-for-it.sh"
#"./wait-for-it.sh","nedrex-db:3306","-t","30", "--","java","-Xmx4g","-jar", "app.jar","--spring.profiles.active=prod","--spring.config.location=./config/server/"
ENTRYPOINT /usr/app/scripts/set-context.sh conf/Catalina/localhost/nedrexweb-backend.xml ; /usr/app/scripts/set-server.sh conf/server.xml ; ../wait-for-it.sh nedrex-db:3306 -t 30 -- bin/catalina.sh run
