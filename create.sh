#!/bin/bash
set -e
docker service create \
--name roxa-sample \
--network my_attachable_net \
--mount source=/opt/roxa-sample/conf,target=/opt/vertxapp/conf \
--publish published=8181,target=8181 \
--constraint 'node.role==worker' \
registry.cn-hangzhou.aliyuncs.com/roxa/roxa-sample:latest