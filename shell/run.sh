#!/bin/bash
set -e
docker run -d \
--name roxa-sample \
--network my_attachable_net \
--mount type=bind,source=/opt/parkletx/conf,target=/opt/vertxapp/conf \
--publish=8181:8181 \
registry.cn-hangzhou.aliyuncs.com/roxa/parkletx:latest
#--publish published=8181,target=8181