#!/bin/bash
set -e
docker run -d \
--name roxa-sample \
--network my_attachable_net \
-v $(pwd)/conf,target=/opt/vertxapp/conf \
-p 8181:8181 \
registry.cn-hangzhou.aliyuncs.com/roxa/roxa-sample:latest