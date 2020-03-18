#!/bin/bash
set -e
docker stop roxa-sample && docker rm roxa-sample
docker pull registry.cn-hangzhou.aliyuncs.com/roxa/roxa-sample:latest
. run.sh