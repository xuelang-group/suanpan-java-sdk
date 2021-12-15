#!/usr/bin/env bash
BASE_PATH=`pwd`
cd $BASE_PATH/dockerimagedir
SOURCE_JAR_PATH=$BASE_PATH/../target/suanpan-uc-1.0-SNAPSHOT.jar
cp -f $SOURCE_JAR_PATH app.jar

currentTime=`date "+%Y%m%d%H%M%S"`
docker_repository=registry.cn-shanghai.aliyuncs.com/shuzhi-amd64/suanpan-uc
docker_tag=1.0.11-weichaiapi

docker build -t "${docker_repository}:${docker_tag}" .
#container_name=xuelanguc_$docker_tag
#docker run -d -p 80:7002 --name ${container_name} -v /root/deploy/xuelang-uc/application.yml:/etc/application.yml:rw ${docker_repository}:${docker_tag}

