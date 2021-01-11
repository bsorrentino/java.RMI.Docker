FROM openjdk:8

MAINTAINER bartolomeo.sorrentino@softphone.it

# unpack  docker-build.jar in /maven
ADD . /


RUN mkdir   /maven/share

VOLUME      /maven/share

WORKDIR     /maven

ENTRYPOINT ["java", "-jar", "@build.finalName@.jar"]


