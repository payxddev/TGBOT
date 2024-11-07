FROM bellsoft/liberica-openjre-alpine:17
VOLUME /tmp
RUN adduser -S spring-user
USER spring-user
COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
