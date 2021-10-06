FROM ubuntu
#
WORKDIR /usr/app
#
RUN apt update --fix-missing
ENV TZ=Europe/Berlin
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
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


RUN apt update --fix-missing && apt install -y gnupg2 \
                                               python3 \
                                               python3-pip


ENV PYTHONPATH=$PYTHONPATH:/usr/local/lib/python3.8/dist-packages/
ENV PYTHON=/usr/bin/python3
RUN pip3 install pipreqs numpy
##
RUN wget https://download.java.net/java/GA/jdk15.0.1/51f4f36ad4ef43e39d0dfdbaf6549e32/9/GPL/openjdk-15.0.1_linux-x64_bin.tar.gz
RUN tar -xzf openjdk-15.0.1_linux-x64_bin.tar.gz
RUN rm openjdk-15.0.1_linux-x64_bin.tar.gz
ENV JAVA_HOME=/usr/app/jdk-15.0.1
ENV PATH=$PATH:$JAVA_HOME/bin
##
##
RUN mkdir graph_tools
WORKDIR /usr/app/scripts/graph_tools
RUN wget https://downloads.skewed.de/graph-tool/graph-tool-2.35.tar.bz2
RUN tar -xf graph-tool-2.35.tar.bz2
WORKDIR /usr/app/scripts/graph_tools/graph-tool-2.35
#
RUN ./configure
RUN make
RUN make install
WORKDIR /usr/app/scripts
RUN rm -rf graph_tools


RUN apt update --fix-missing && apt install libreadline-gplv2-dev libncursesw5-dev libssl-dev libsqlite3-dev tk-dev libgdbm-dev libc6-dev libbz2-dev

#Set up python3.7 for domino
WORKDIR /usr/app
RUN mkdir python37
WORKDIR /usr/app/python37
RUN wget https://www.python.org/ftp/python/3.7.12/Python-3.7.12.tgz
RUN tar -xf Python-3.7.12.tgz
WORKDIR /usr/app/python37/Python-3.7.12
RUN ./configure --prefix=/usr/local/python3.7.12 --enable-optimizations --enable-shared
RUN make
RUN make install
#RUN ln -s /usr/local/python3.7.12/bin/python3 /usr/local/python3.7.12/bin/python3.7
RUN ln -s /usr/local/python3.7.12/bin/pip3 /usr/local/python3.7.12/bin/pip3.7

ENV LD_LIBRARY_PATH=/usr/local/python3.7.12/lib:$LD_LIBRARY_PATH

#WORKDIR /usr/app
#
#RUN wget https://ftp-stud.hs-esslingen.de/pub/Mirrors/ftp.apache.org/dist/tomcat/tomcat-8/v8.5.63/bin/apache-tomcat-8.5.63.tar.gz
#RUN tar -xzf apache-tomcat-8.5.63.tar.gz
#RUN rm apache-tomcat-8.5.63.tar.gz
#RUN mv apache-tomcat-8.5.63 apache-tomcat
#RUN rm -rf apache-tomcat/webapps/*
