FROM openjdk:8

MAINTAINER bartolomeo.sorrentino@softphone.it

# unpack  docker-build.jar in /maven
ADD . /

WORKDIR /maven

RUN mkdir target

EXPOSE 8081

#ENTRYPOINT ["java", "-jar", "@build.finalName@.jar", ""]
CMD ["./exec.sh", "@build.finalName@.jar"]
