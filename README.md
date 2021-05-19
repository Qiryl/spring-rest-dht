# spring-rest-dht

## Launch nodes
By default, the system type is set to `sharding`.
But if you want you can set this value manually to `sharding` or `replication`
using flags accordingly `--dht.type=sharding` or `--dht.type=replication`.
```
java -jar build/libs/dht-0.0.1-SNAPSHOT.jar --server.port=8080
java -jar build/libs/dht-0.0.1-SNAPSHOT.jar --server.port=8081
java -jar build/libs/dht-0.0.1-SNAPSHOT.jar --server.port=8082
```

## Join nodes
#### Join first two nodes
```
curl -H "Content-Type: application/json" -X POST -d '{"ip":"localhost", "port":"8081"}' http://localhost:8080/join 
```

#### Add the last one to all the rest
```
curl -H "Content-Type: application/json" -X POST -d '{"ip":"localhost", "port":"8080"}' http://localhost:8082/join
curl -H "Content-Type: application/json" -X POST -d '{"ip":"localhost", "port":"8081"}' http://localhost:8082/join
```
As a result, each node will be connected to all others.

## Upload file
If you use `sharding` then system will determine which node is responsible for storing this file.  
If you use `replication` then file will be uploaded to all connected nodes.
``` 
curl -F "file=@file.txt" http://localhost:8080/put
```

## Delete node
If you use `sharding` then system will distribute all values stored on the deleted node among all other nodes.
After distribution node will be deleted. When using `replication`, the node will simply be deleted.
```
curl -X DELETE http://localhost:8080
```
