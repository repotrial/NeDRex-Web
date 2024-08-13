FROM andimajore/nedrex_repo:server_base

ENV JAVA_HOME=/usr/app/jdk-15.0.1
ENV PATH=$PATH:$JAVA_HOME/bin

RUN mkdir cache scripts
WORKDIR /usr/app/scripts
COPY scripts/ ./

RUN apt update
RUN apt install -y software-properties-common

RUN pip install --upgrade pip

RUN mamba create -n default python=3.9

RUN pipreqs ./
RUN cat requirements.txt | grep -v "graph_tool" | grep -v "pcst_fast" | grep -v 'bicon' | grep -v 'networkx' | grep -v 'scipy' > reqs.txt
RUN /opt/conda/envs/default/bin/pip install -r reqs.txt

RUN git clone https://github.com/AndiMajore/BiCoN.git
WORKDIR BiCoN
RUN /opt/conda/envs/default/bin/python setup.py install
WORKDIR /usr/app
RUN rm -rf BiCoN

RUN mamba install -n default networkx==2.5.1 pyparsing==2.4.6 numpy==1.26.4 scipy==1.12.0
RUN mamba install -n default -c conda-forge graph-tool

RUN mamba install -n domino_env pyparsing==2.4.7 numpy==1.18.1

RUN mamba clean --all -y

WORKDIR /usr/app

COPY config/ config/
COPY src/main/resources/application-prod.properties config/server/
RUN mkdir cache

COPY target/nedrexweb-backend.war ./

COPY scripts/wait-for-it.sh wait-for-it.sh

ENTRYPOINT ./wait-for-it.sh nedrex-web-db:3306 -t 30 -- java -Xmx16g -jar nedrexweb-backend.war

