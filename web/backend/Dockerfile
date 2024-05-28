FROM andimajore/nedrex_repo:server_base as production-stage


ENV PYTHON=/usr/bin/python3
ENV PYTHONPATH=$PYTHONPATH:/usr/local/lib/python3.8/dist-packages/
ENV JAVA_HOME=/usr/app/jdk-15.0.1
ENV PATH=$PATH:$JAVA_HOME/bin

RUN mkdir cache scripts
WORKDIR /usr/app/scripts
COPY scripts/ ./

RUN pip install --upgrade pip

RUN pipreqs ./
RUN cat requirements.txt | grep -v "graph_tool" | grep -v "pcst_fast" | grep -v 'bicon' | grep -v 'networkx' > reqs.txt
RUN pip3 install -r reqs.txt

RUN git clone https://github.com/AndiMajore/BiCoN.git
WORKDIR BiCoN
RUN python3 setup.py install
WORKDIR /usr/app
RUN rm -rf BiCoN

RUN pip3 uninstall -y pyparsing
RUN pip3 install networkx==2.5.1 pyparsing==2.4.6

RUN apt update
RUN apt install -y software-properties-common
#RUN apt-key adv --keyserver keys.openpgp.org --recv-key 612DEFB798507F25
#RUN add-apt-repository "deb [ arch=amd64 ] https://downloads.skewed.de/apt focal main"
RUN #apt update && apt install -y python3-graph-tool
RUN conda install -c conda-forge graph-tool

#ENV LD_LIBRARY_PATH=/opt/conda/envs/domino_env/lib:$LD_LIBRARY_PATH
#ENV PYTHONPATH=$PATHONPATH:/opt/conda/envs/domino_env/lib/python3.7/site-packages/
#ENV PYTHON=/opt/conda/envs/domino_env/bin/python3.7
#ENV PATH=$PATH:/opt/conda/envs/domino_env/bin

RUN  /opt/conda/envs/domino_env/bin/pip install domino-python numpy==1.18.1 matplotlib pandas==1.0.1 networkx==2.4
RUN  /opt/conda/envs/domino_env/bin/pip uninstall -y pyparsing
RUN  /opt/conda/envs/domino_env/bin/pip install pyparsing==2.4.7

WORKDIR /usr/app

COPY config/ config/
COPY src/main/resources/application-prod.properties config/server/
RUN mkdir cache

COPY target/nedrexweb-backend.war ./

COPY scripts/wait-for-it.sh wait-for-it.sh

ENTRYPOINT ./wait-for-it.sh nedrex-web-db:3306 -t 30 -- java -Xmx16g -jar nedrexweb-backend.war

