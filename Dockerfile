FROM eclipse-temurin:21-jre-jammy

ARG PROFILE

ENV TZ=Asia/Seoul
ENV KTOR_ENV=$PROFILE

WORKDIR /app

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY build/libs/e-receipt-api-fat.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -DKTOR_ENV=${KTOR_ENV} -jar app.jar -config=application.conf -config=application-${KTOR_ENV}.conf"]