#!/bin/bash
set -e

docker service create \
--name mysqld \
--config source=mysql_charset.cnf,target=/etc/mysql/conf.d/mysql_charset.cnf \
--network my_attachable_net \
--mount source=vol_mysqld,target=/var/lib/mysql \
--publish published=3306,target=3306 \
--constraint 'node.role==manager' \
-e MYSQL_ROOT_PASSWORD=pass4pass \
-e TZ=Asia/Shanghai \
mysql:5.7
