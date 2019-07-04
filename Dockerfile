FROM openjdk:8-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=build/dependency
COPY ${DEPENDENCY}/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Xmx2048m -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar" ]
