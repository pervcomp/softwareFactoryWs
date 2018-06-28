FROM ubuntu:18.04

#MAINTAINER Ivan Krizsan, https://github.com/krizsan

RUN apt-get update && \

    apt-get upgrade -y && \

    apt-get install -y  software-properties-common && \

#   add-apt-repository ppa:webupd8team/java -y && \

#    apt-get update && \

#    echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \

#    apt-get install -y oracle-java8-installer && \

     apt-get install -y  openjdk-8-jdk && \

     apt-get install -y  wget &&  \

     apt-get install -y  zip unzip


RUN apt-get install -y git
RUN export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
VOLUME /tmp
EXPOSE 8080
ADD target/spring-softwarefactoryws-1.5.1.war  app.jar
ENV JAVA_OPTS=""
RUN ln -fs /usr/share/zoneinfo/Europe/Rome /etc/localtime
#&& dpkg-reconfigure -f noninteractive tzdata

#RUN apt-get install git

#RUN java -version

#RUN apk add wget ca-certificates openssl-dev --update-cache
RUN  wget https://sonarsource.bintray.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-3.0.3.778-linux.zip
RUN ls
#RUN apt-get install -y  zip unzip
RUN  unzip  sonar-scanner-cli-3.0.3.778-linux.zip
RUN mv sonar-scanner-3.0.3.778-linux sonar-scanner
RUN mv sonar-scanner /usr/bin
RUN test=sonar.host.url=https://sonar.rd.tut.fi
RUN echo $test >> /usr/bin/sonar-scanner/conf/sonar-scanner.properties

ENV PATH="/usr/bin/sonar-scanner/bin:${PATH}"
RUN sonar-scanner -h


ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]

