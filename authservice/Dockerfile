FROM ubuntu:22.04

# Avoid prompts from apt
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y openjdk-21-jdk maven curl git && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN java -version
RUN mvn -v

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/auth-0.0.1-SNAPSHOT.jar" ]