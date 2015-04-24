FROM zalando/openjdk:8u40-b09-4


RUN mkdir /app
RUN mkdir /app/config

WORKDIR /app

ADD target/zmon-data-service-1.0-SNAPSHOT.jar /app/zmon-data-service.jar
ADD config/application.yaml /app/config/application.yaml

EXPOSE 8086

CMD ["java","-jar","zmon-data-service.jar"]

