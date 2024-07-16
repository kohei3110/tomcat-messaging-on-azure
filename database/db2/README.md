## Db2 インスタンス作成

docker pull --platform=linux/amd64 icr.io/db2_community/db2 

vi .env_list

```
LICENSE=accept
DB2INSTANCE=db2inst1
DB2INST1_PASSWORD=password
DBNAME=testdb
BLU=false
ENABLE_ORACLE_COMPATIBILITY=false
UPDATEAVAIL=NO
TO_CREATE_SAMPLEDB=true
REPODB=false
IS_OSXFS=true
PERSISTENT_HOME=true
HADR_ENABLED=false
ETCD_ENDPOINT=
ETCD_USERNAME=
ETCD_PASSWORD=
```

```
docker run -itd -h db2server --name db2server --restart=always \
--privileged=true -p 50000:50000 --env-file .env_list \
-v v_db2server:/database --platform=linux/amd64 \
--shm-size=2g \
icr.io/db2_community/db2
```

```
docker exec -it db2server /bin/bash
su - db2inst1
db2start
db2 list db directory
```