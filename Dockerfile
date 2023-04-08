FROM eclipse-temurin:19-jdk

COPY ./build/server.jar server.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=9010", "-Dcom.sun.management.jmxremote.rmi.port=9010", "-Dcom.sun.management.jmxremote.local.only=false", "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false", "-Djava.rmi.server.hostname=127.0.0.1", "-XX:InitialRAMPercentage=80.0", "-XX:MinRAMPercentage=80.0", "-XX:MaxRAMPercentage=80.0", "-XX:ActiveProcessorCount=2", "-jar", "server.jar"]
