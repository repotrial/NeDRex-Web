FROM andimajore/nedrex_repo:server_base

ENV PYTHONPATH=$PYTHONPATH:/usr/local/lib/python3.8/dist-packages/
ENV PYTHON=/usr/bin/python3
ENV JAVA_HOME=/usr/app/jdk-15.0.1
ENV PATH=$PATH:$JAVA_HOME/bin

RUN mkdir scripts cache
WORKDIR /usr/app/scripts
COPY scripts* .
#RUN ./diamond_setup.sh

RUN pipreqs ./
RUN cat requirements.txt | grep -v "graph_tool" > reqs.txt
RUN pip3 install -r reqs.txt
RUN pip3 uninstall -y networkx
RUN pip3 install networkx==2.3


WORKDIR /usr/app
COPY scripts/wait-for-it.sh wait-for-it.sh

COPY config/db_config.json config/db/
COPY target/reposcapeweb-backend-0.0.1-SNAPSHOT-spring-boot.jar app.jar
COPY src/main/resources/application-prod.properties config/server/
RUN mkdir cache


EXPOSE 8090

#ENTRYPOINT tail -f /dev/null
ENTRYPOINT ["./wait-for-it.sh","nedrex-db:3306","-t","30", "--","java","-Xmx4g","-jar", "app.jar","--spring.profiles.active=prod","--spring.config.location=./config/server/"]
#ENTRYPOINT ["./wait-for-it.sh","nedrex-db:3307","-t","30", "--","java", "-Dspring.profiles.active=prod","-jar", "app.jar"]