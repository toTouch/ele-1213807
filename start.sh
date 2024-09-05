#!/bin/bash
HEALT_URI=/electricityCabinet/actuator/health
APP_NAME=saas-electricity
CONTAINER_PORT=28080
if [ $(($BUILD_NUMBER % 2)) -ne 0 ]; then
  CONTAINER_PORT=28081
fi
echo "******************开始构建新版本镜像****************************"
docker build -f Dockerfile -t $REGISTRY/${APP_NAME}:$BUILD_NUMBER .
echo "******************构建新版本镜像完成****************************"

echo "******************开始推送新版本镜像****************************"
echo "$DOCKER_PWD_VAR"|docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin
docker push $REGISTRY/${APP_NAME}:$BUILD_NUMBER
echo "******************推送新版本镜像完成****************************"

echo "******************开始执行滚动更新****************************"
CONTAINER_ID=$(docker ps -a|grep ${APP_NAME}|awk '{print $1}')
echo "*************************获取上一个版本的容器id: ${CONTAINER_ID}"
echo "******************开始启动新版本容器****************************"
docker run -d -p ${CONTAINER_PORT}:8080 \
--net="${NET_WORK}" \
${LINK_MYSQL} -e "MYSQL_ADDR=mysql" \
${LINK_NACOS} -e "NACOS_ADDR=nacos" \
${LINK_REDIS} -e "REDIS_ADDR=redis" \
${LINK_CH} -e "CLICKHOUSE_ADDR=clickhouse" \
${LINK_XXL} -e "XXL_ADDR=xxl" \
${LINK_MQ} -e "ROCKETMQ_ADDR=rocketmq" \
-e ENDPOINT_ADDR="saas_dev.tinygpt.cn/api" \
-v /opt/soft/env-data/logs/saas-electricity:/app/logs \
--name ${APP_NAME}-$BUILD_NUMBER \
$REGISTRY/${APP_NAME}:$BUILD_NUMBER
curl http://${LOCAL_EXPORT_IP}:${CONTAINER_PORT}${HEALT_URI}
RESULT_CODE=$(echo $?)
WAIT_COUNT=0
while [ $RESULT_CODE -ne 0 ]; do
    if [ $WAIT_COUNT -gt 25 ]; then
        echo "******************滚动更新超时[$(($WAIT_COUNT * 3))/s],请排查****************************"
        echo "******************Container Name****************************"
        echo "******************[${APP_NAME}-$BUILD_NUMBER]****************************"
        exit 1
    fi
    sleep 3
    echo "************************正在启动中******************"
    curl http://${LOCAL_EXPORT_IP}:${CONTAINER_PORT}${HEALT_URI}
    RESULT_CODE=$(echo $?)
    echo "************************正在启动中获取结果: $RESULT_CODE ******************"
    ((WAIT_COUNT++))
done
RESULT_DATA=$(curl http://${LOCAL_EXPORT_IP}:${CONTAINER_PORT}${HEALT_URI} | grep "UP")
if [ -n "$RESULT_DATA" ]; then
    echo "******************新版本容器启动成功****************************"
    if [ -n "$CONTAINER_ID" ]; then
      echo "******************开始删除旧版本容器****************************"
        docker stop $CONTAINER_ID
        docker rm $CONTAINER_ID
      echo "******************删除旧版本成功****************************"
    fi
    echo "******************滚动更新完成****************************"
    exit 0
else
    echo "******************滚动更新失败,请排查****************************"
    echo "******************Container Name****************************"
    echo "******************[${APP_NAME}-$BUILD_NUMBER]****************************"
    exit 1
fi
