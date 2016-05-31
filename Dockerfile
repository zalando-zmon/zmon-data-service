FROM registry.opensource.zalan.do/stups/openjdk:8u91-b14-1-22

EXPOSE 8086

COPY target/zmon-data-service-1.0-SNAPSHOT.jar /zmon-data-service.jar
COPY target/scm-source.json /

CMD java $JAVA_OPTS $(java-dynamic-memory-opts) -jar /zmon-data-service.jar
