FROM openjdk:11

MAINTAINER peaklee<wxblifeng@163.com>

ENV WORK_DIR /app
ENV NACOS_ADDR nacos
ENV NACOS_PORT 8848
ENV MYSQL_ADDR mysql
ENV MYSQL_PORT 3306
ENV MYSQL_USER root
ENV MYSQL_PASSWORD mysql_YptKyz
ENV MYSQL_DATABASE saas_electricity
ENV REDIS_ADDR redis
ENV REDIS_PORT 6379
ENV CLICKHOUSE_ADDR clickhouse
ENV CLICKHOUSE_PORT 8123
ENV CLICKHOUSE_USER admin
ENV CLICKHOUSE_PASSWORD 123456
ENV CLICKHOUSE_DATABASE test
ENV ROCKETMQ_ADDR rocketmq
ENV ROCKETMQ_PORT 9876
ENV XXL_ADDR xxl-job
ENV XXL_PORT 3080
ENV ENDPOINT_ADDR endpoint

WORKDIR $WORK_DIR

RUN mkdir -p $WORK_DIR
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \&& echo 'Asia/Shanghai' >/etc/timezone

VOLUME /tmp

VOLUME /app/logs

ADD target/xiliulou-electricity-cabinet.jar $WORK_DIR/boot.jar

ENTRYPOINT java -Dspring.profiles.active=docker -Djava.security.egd=file:/dev/./urandom -jar $WORK_DIR/boot.jar

EXPOSE 8080
