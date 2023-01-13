FROM amazoncorretto:17.0.5-alpine
ARG PROJECT_VERSION
ENV RELEASE_VERSION=$PROJECT_VERSION
ARG JAR_FILE=./target/klaw-${RELEASE_VERSION}.jar
COPY ${JAR_FILE} klaw-cluster-api-${RELEASE_VERSION}.jar
ENTRYPOINT java -jar klaw-cluster-api-${RELEASE_VERSION}.jar
