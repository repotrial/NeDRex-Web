FROM andimajore/mamba_mantic:latest
#
WORKDIR /usr/app
#
RUN apt update --fix-missing
RUN apt install -y wget \
                   pkg-config \
                   diffutils \
                   unzip \
                   curl \
                   libboost-all-dev \
                   libgmp-dev \
                   libcgal-dev \
                   libcairomm-1.0-dev \
                   python3-cairo-dev \
                   libsparsehash-dev


RUN apt update --fix-missing && apt install -y gnupg2


#ENV PYTHONPATH=$PYTHONPATH:/usr/local/lib/python3.8/dist-packages/
#ENV PYTHON=/usr/bin/python3
RUN mamba install -y -c conda-forge pipreqs numpy graph-tool==2.58
##
RUN wget https://download.java.net/java/GA/jdk15.0.1/51f4f36ad4ef43e39d0dfdbaf6549e32/9/GPL/openjdk-15.0.1_linux-x64_bin.tar.gz
RUN tar -xzf openjdk-15.0.1_linux-x64_bin.tar.gz
RUN rm openjdk-15.0.1_linux-x64_bin.tar.gz
ENV JAVA_HOME=/usr/app/jdk-15.0.1
ENV PATH=$PATH:$JAVA_HOME/bin
##
##
#RUN mkdir graph_tools
#WORKDIR /usr/app/scripts/graph_tools
#RUN wget https://downloads.skewed.de/graph-tool/graph-tool-2.35.tar.bz2
#RUN tar -xf graph-tool-2.35.tar.bz2
#WORKDIR /usr/app/scripts/graph_tools/graph-tool-2.35
#
#RUN ./configure
#RUN make
#RUN make install
WORKDIR /usr/app/scripts
#RUN rm -rf graph_tools


RUN apt update --fix-missing && apt install -y libreadline-dev libncursesw5-dev libssl-dev libsqlite3-dev tk-dev libgdbm-dev libc6-dev libbz2-dev

RUN apt-get install -y git

RUN mamba create -y -c conda-forge -n domino_env python=3.9
RUN mamba env list
RUN /opt/conda/envs/domino_env/bin/python -m pip install https://github.com/Shamir-Lab/DOMINO/archive/refs/tags/v1.0.0.zip


#RUN pip3.7 install domino-python numpy==1.18.1 matplotlib pandas==1.0.1 networkx==2.4
#RUN pip3.7 uninstall -y pyparsing
#RUN pip3.7 install pyparsing==2.4.7
#Set up python3.7 for domino
#WORKDIR /usr/app
#RUN mkdir python37
#WORKDIR /usr/app/python37
#RUN wget https://www.python.org/ftp/python/3.7.12/Python-3.7.12.tgz
#RUN tar -xf Python-3.7.12.tgz
#WORKDIR /usr/app/python37/Python-3.7.12
#RUN ./configure --prefix=/usr/local/python3.7.12 --enable-optimizations --enable-shared
#RUN make
#RUN make install
