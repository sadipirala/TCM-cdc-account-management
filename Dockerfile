FROM amazoncorretto:8
CMD apt-get install awscli.
COPY credentials /root/.aws/credentials
WORKDIR /app
COPY target/cdcam.jar cdcam.jar
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:MaxRAMFraction=2", "-Dspring.profiles.active=${SPRING.PROFILES.ACTIVE}", "-jar", "cdcam.jar"]