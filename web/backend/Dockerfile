FROM andimajore/nedrex_repo:server_base as production-stage


#ENV PYTHON=/usr/bin/python3
#ENV PYTHONPATH=$PYTHONPATH:/usr/local/lib/python3.8/dist-packages/
ENV JAVA_HOME=/usr/app/jdk-15.0.1
ENV PATH=$PATH:$JAVA_HOME/bin

RUN mkdir cache scripts
WORKDIR /usr/app/scripts
COPY scripts/ ./

RUN pipreqs ./
RUN cat requirements.txt | grep -v "graph_tool" | grep -v "pcst_fast" | grep -v "matplotlib" | grep -v "bicon" | grep -v libglib> reqs.txt
RUN mamba install -c conda-forge --file reqs.txt

RUN mamba uninstall -y networkx numpy pyparsing
RUN pip install bicon
RUN mamba install -c conda-forge networkx==2.5.1 numpy==1.21.0 pyparsing==2.4.6 matplotlib libglib

RUN #apt update && apt install -y software-properties-common
RUN #apt-key adv --keyserver keys.openpgp.org --recv-key 612DEFB798507F25
RUN #add-apt-repository "deb [ arch=amd64 ] https://downloads.skewed.de/apt focal main"
RUN #apt update
#RUN apt install -y python3-graph-tool

#ENV LD_LIBRARY_PATH=/usr/local/python3.7.12/lib:$LD_LIBRARY_PATH
#ENV PYTHONPATH=$PATHONPATH:/usr/lib/python3.7/dist-packages/
#ENV PYTHON=/usr/local/python3.7.12/bin/python3.7
#ENV PATH=$PATH:/usr/local/python3.7.12/bin

#RUN pip3.7 install domino-python numpy==1.18.1 matplotlib pandas==1.0.1 networkx==2.4
#RUN pip3.7 uninstall -y pyparsing
#RUN pip3.7 install pyparsing==2.4.7

WORKDIR /usr/app

COPY config/ config/
COPY src/main/resources/application-prod.properties config/server/
RUN mkdir cache

COPY target/nedrexweb-backend.war ./

COPY scripts/wait-for-it.sh wait-for-it.sh

#ENTRYPOINT ./wait-for-it.sh nedrex-web-db:3306 -t 30 -- apache-tomcat/bin/catalina.sh run Dspring.profiles.active=prod
ENTRYPOINT ./wait-for-it.sh nedrex-web-db:3306 -t 30 -- java -Xmx16g -jar nedrexweb-backend.war

