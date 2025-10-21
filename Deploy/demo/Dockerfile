FROM openjdk:21-jdk

WORKDIR /app

COPY target/demo-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8081

ENTRYPOINT ["java" , "-jar" , "app.jar"]