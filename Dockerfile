FROM java:8
FROM maven:alpine

# image layer
WORKDIR /app
ADD pom.xml /app
RUN mvn verify clean --fail-never

# Image layer: with the application
COPY . /app
RUN mvn -v
RUN mvn clean install -DskipTests
EXPOSE 8081
ADD ./target/oracle-enterprise-demo-0.0.1-SNAPSHOT.jar /run/
ENTRYPOINT ["java","-jar","/run/oracle-enterprise-demo-0.0.1-SNAPSHOT.jar"]
