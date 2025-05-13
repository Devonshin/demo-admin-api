FROM eclipse-temurin:21-jre-jammy

ARG PROFILE
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_KEY

ENV TZ=Asia/Seoul
ENV KTOR_ENV=$PROFILE
ENV JAVA_OPTS="-Daws.accessKeyId=$AWS_ACCESS_KEY_ID \
               -Daws.secretKey=$AWS_SECRET_KEY"
WORKDIR /app

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY build/libs/e-receipt-api-fat.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -DKTOR_ENV=${KTOR_ENV} -jar app.jar -config=application.conf -config=application-${KTOR_ENV}.conf"]