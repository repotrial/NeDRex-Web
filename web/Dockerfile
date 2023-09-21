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

RUN mamba install -y -c conda-forge pipreqs numpy graph-tool==2.58
##
RUN wget https://download.java.net/java/GA/jdk20.0.2/6e380f22cbe7469fa75fb448bd903d8e/9/GPL/openjdk-20.0.2_linux-x64_bin.tar.gz
RUN tar -xzf openjdk-20.0.2_linux-x64_bin.tar.gz
RUN rm openjdk-20.0.2_linux-x64_bin.tar.gz
ENV JAVA_HOME=/usr/app/jdk-20.0.2
ENV PATH=$PATH:$JAVA_HOME/bin

WORKDIR /usr/app/scripts


RUN apt update --fix-missing && apt install -y libreadline-dev libncursesw5-dev libssl-dev libsqlite3-dev tk-dev libgdbm-dev libc6-dev libbz2-dev

RUN apt-get install -y git

RUN mamba create -y -c conda-forge -n domino_env python=3.7
RUN /opt/conda/envs/domino_env/bin/python -m pip install domino-python pcst-fast numpy==1.18.1 matplotlib pandas==1.0.1 networkx==2.4
RUN /opt/conda/envs/domino_env/bin/python -m pip uninstall -y pyparsing
RUN /opt/conda/envs/domino_env/bin/python -m pip install pyparsing==2.4.7


