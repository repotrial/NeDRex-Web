FROM andimajore/nedrex_repo:server_base as production-stage

ENV JAVA_HOME=/usr/app/jdk-20.0.2
ENV PATH=$PATH:$JAVA_HOME/bin

RUN mkdir cache scripts
WORKDIR /usr/app/scripts
COPY scripts/ ./

RUN pipreqs ./
RUN cat requirements.txt | grep -v "graph_tool" | grep -v "pcst_fast" | grep -v "matplotlib" | grep -v "bicon" | grep -v "pandas" | grep -v "libglib" | grep -v "networkx"> reqs.txt
RUN mamba install -c conda-forge -c anaconda --file reqs.txt

RUN mamba uninstall -y networkx numpy pyparsing
RUN pip install bicon
RUN mamba install -y -c conda-forge networkx==2.5.1 numpy==1.21.0 pyparsing==2.4.6 matplotlib libglib pandas graph-tool

WORKDIR /usr/app

COPY config/ config/
COPY src/main/resources/application-prod.properties config/server/
RUN mkdir cache

COPY target/nedrexweb-backend.war ./

COPY scripts/wait-for-it.sh wait-for-it.sh
EXPOSE 8022
ENTRYPOINT ./wait-for-it.sh nedrex-web-db:3306 -t 30 -- java -Xmx16g -jar nedrexweb-backend.war

