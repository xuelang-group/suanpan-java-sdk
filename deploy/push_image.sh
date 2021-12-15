#!/usr/bin/env bash
if [ $# != 3 ]; then
# echo "USAGE: $0 xuelang-uc imageId 0.1.1"
echo "USAGE: $0 suanpan-uc dfdf2238c968 1.0.11-weichaiapi"
exit 1
fi
docker login registry.cn-shanghai.aliyuncs.com/shuzhi-amd64 -u suanpan-dev@xuelangyun -p xlszWork2018!
sys_name=$1
image_id=$2
version=$3
docker tag $2 registry.cn-shanghai.aliyuncs.com/shuzhi-amd64/$1:$3
docker push registry.cn-shanghai.aliyuncs.com/shuzhi-amd64/$1:$3
echo "$1:$3" >> version.txt