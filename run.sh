#!/bin/bash
set -e
docker run -d \
--name roxa-sample \
--network my_attachable_net \
--mount source=/opt/roxa-sample/conf,target=/opt/vertxapp/conf \
--publish published=8181,target=8181 \
registry.cn-hangzhou.aliyuncs.com/roxa/roxa-sample:latest