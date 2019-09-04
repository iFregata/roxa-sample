# Roxa Sample Project

## Eclipse Vert.x live reload

New run configuration

The main class: `io.vertx.core.Launcher`

The arguments

```
run io.roxa.tutor.sample --redeploy=**/*.java --launcher-class=io.vertx.core.Launcher -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory
```

The Env: `APP_LOG_LEVEL=debug`

## 部署说明

### Docker Swarm

```
docker swarm init
```

### MySQL Service

Create MySQL docker volume

```
docker volume create vol_mysqld
```

Create docker network

```
docker network create -d overlay --attachable my_attachable_net
```

Create MySQL Config

```
docker config create mysqld_charset.cnf mysqld_charset.cnf
```

Create MySQL service

```
docker service create \
--name mysqld \
--config source=mysqld_charset.cnf,target=/etc/mysql/conf.d/mysqld_charset.cnf \
--network my_attachable_net \
--mount source=vol_mysqld,target=/var/lib/mysql \
--publish published=3306,target=3306 \
--constraint 'node.role==manager' \
-e MYSQL_ROOT_PASSWORD=pass4pass \
-e TZ=Asia/Shanghai \
mysql:5.7
```

Connect to MySQL container

```
docker run -it --rm mysql:5.7 mysql -uroot -hmysqld -p
```

Create schema

```
create schema mystore;

use mystore;

create table product(
  id varchar(32) not null primary key,
  name varchar(64) not null,
  description varchar(255),
  price int not null default 0,
  date_created bigint not null,
  date_modified bigint not null
);
```

##### RESTful API

Create Product

```
http -v :8181/mystore/products id=1001 name=iphonex description='手机' price:=868800
```

Update Product

```
http -v :8181/mystore/products id=1001 name=iphonex description='手机2' price:=868800
```

Find Product

```
http -v :8181/mystore/products/1001
```

List Product

```
http -v :8181/mystore/products
```

Remove Product

```
http -v DELETE :8181/mystore/products/1001
```