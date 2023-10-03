FROM amazoncorretto:17.0.7-alpine3.15
CMD apt-get install awscli.
COPY credentials /root/.aws/credentials
WORKDIR /app
COPY target/cdcam.jar cdcam.jar
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseContainerSupport", "-XX:MaxRAMFraction=2", "-Dspring.profiles.active=${SPRING.PROFILES.ACTIVE}", "-jar", "cdcam.jar"]