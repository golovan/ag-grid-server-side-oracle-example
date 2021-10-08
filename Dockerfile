FROM java:8
FROM maven:alpine
WORKDIR /app
COPY . /app
RUN mvn -v
RUN mvn clean install -DskipTests
EXPOSE 8081
ADD ./target/oracle-enterprise-demo-0.0.1-SNAPSHOT.jar /run/
ENTRYPOINT ["java","-jar","/run/oracle-enterprise-demo-0.0.1-SNAPSHOT.jar"]