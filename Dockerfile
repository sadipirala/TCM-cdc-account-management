#FROM amazoncorretto:17.0.7-alpine3.15
#CMD apt-get install awscli.
#COPY credentials /root/.aws/credentials
#WORKDIR /app
#COPY target/cdcam.jar cdcam.jar
#ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseContainerSupport", "-XX:MaxRAMFraction=2", "-Dspring.profiles.active=${SPRING.PROFILES.ACTIVE}", "-jar", "cdcam.jar"]
#FROM amazoncorretto:21-alpine-jdk as build
#CMD apt-get install awscli.
#COPY credentials /root/.aws/credentials
#WORKDIR /app
#COPY . .
#RUN chmod +x gradlew
#RUN ./gradlew clean build --stacktrace
#RUN ls -lrt
#RUN cd build;ls -lrt
#RUN cd build/libs;ls -lrt


FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

COPY build/libs/cdcam.jar cdcam.jar
COPY script/upload_heapdump_gc_s3.sh /app/upload_heapdump_gc_s3.sh
RUN chmod 777 /app/upload_heapdump_gc_s3.sh
RUN apk update && apk add curl && apk add wget && apk add bash
RUN apk add --no-cache aws-cli
RUN apk --update --no-cache add \
    python3 \
    py3-pip \
    jq \
    bash \
    git \
    groff \
    less \
    mailcap \
    && apk del py3-pip \
    && apk add py3-six \
    && rm -rf /var/cache/apk/* /root/.cache/pip/*

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=70", "-XX:+UseG1GC", "-XX:+UseStringDeduplication", "-Xlog:gc*:file=/var/log/gc-%t.log:time,uptime,tid,tags,level:filecount=10,filesize=10M", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/var/log/oom.hprof", "-XX:OnOutOfMemoryError=/app/upload_heapdump_gc_s3.sh", "-XshowSettings:vm", "-XX:+PrintFlagsFinal", "-XX:+UnlockDiagnosticVMOptions", "-jar", "cdcam.jar"]