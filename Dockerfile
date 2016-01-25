FROM registry.opensource.zalan.do/stups/openjdk:8u66-b17-1-9

RUN mkdir /app
RUN mkdir /app/config

WORKDIR /app

ADD target/zmon-data-service-1.0-SNAPSHOT.jar /app/zmon-data-service.jar
ADD config/application.yaml /app/config/application.yaml

EXPOSE 8086

CMD ["java","-jar","zmon-data-service.jar"]
