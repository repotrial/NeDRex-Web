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
                   gnupg2 \
                   libboost-all-dev \
                   libgmp-dev \
                   libcgal-dev \
                   libcairomm-1.0-dev \
                   python3-cairo-dev \
                   libsparsehash-dev \
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