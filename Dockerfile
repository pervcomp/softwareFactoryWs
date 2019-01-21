FROM maven:3-jdk-8 as build
MAINTAINER Jukka-Pekka Venttola, https://github.com/venttola

WORKDIR /build 
COPY . /build

#RUN apt-get install -y git
#RUN export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
RUN mvn compile && mvn package


FROM openjdk:8-jre-alpine

RUN addgroup -S webservice && \
    adduser -S -h /app -G webservice webservice

RUN apk update && apk add \
  git \
  unzip

EXPOSE 8080
EXPOSE 8090
EXPOSE 9002
VOLUME /tmp

WORKDIR /var/opt
RUN  wget https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-3.3.0.1492-linux.zip
RUN  unzip  sonar-scanner-cli-3.3.0.1492-linux.zip && \
  rm sonar-scanner-cli-3.3.0.1492-linux.zip
RUN mv sonar-scanner-3.3.0.1492-linux sonar-scanner && \
 cd sonar-scanner && \ 
 ln -s $PWD/bin/sonar-scanner /usr/bin/sonar-scanner && \
 ln -s $PWD/bin/sonar-scanner-debug /usr/bin/sonar-scanner-debug
#Link Java correctly for sonar-scanner
RUN rm sonar-scanner/jre/bin/java
RUN ln -s /usr/bin/java sonar-scanner/jre/bin/java

ENV JAVA_OPTS=""
VOLUME /tmp
RUN ln -fs /usr/share/zoneinfo/Europe/Rome /etc/localtime
COPY --from=build /build/target/webservice-1.5.1.war /app/app.jar


ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar" ]

