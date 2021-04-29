# spring-rest-dht
## Join nodes
#### Launching nodes 
    # java -jar build/libs/dht-0.0.1-SNAPSHOT.jar --server.port=8080
    # java -jar build/libs/dht-0.0.1-SNAPSHOT.jar --server.port=8081
    # java -jar build/libs/dht-0.0.1-SNAPSHOT.jar --server.port=8082
#### Join first two nodes
    # curl -H "Content-Type: application/json" -X POST -d '{"ip":"localhost", "port":"8081"}' http://localhost:8080/join 
#### Add the last one to all the rest
    # curl -H "Content-Type: application/json" -X POST -d '{"ip":"localhost", "port":"8080"}' http://localhost:8082/join
    # curl -H "Content-Type: application/json" -X POST -d '{"ip":"localhost", "port":"8081"}' http://localhost:8082/join

As a result, each node will be connected to all others 
