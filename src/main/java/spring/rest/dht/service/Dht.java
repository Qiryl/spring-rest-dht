package spring.rest.dht.service;

import spring.rest.dht.model.Address;
import spring.rest.dht.model.Data;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface Dht {

    void joinNode(Address address);
    Set<Address> getJoinedNodes();
    void putValue(Data data);
    String getValue(String key);
    ConcurrentHashMap<String, String> getAllValues();
    void removeJoinedNode(Address address);
    void delete();

}
