package spring.rest.dht.service;

import spring.rest.dht.model.Address;
import spring.rest.dht.model.Data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface Node {

    public String getId();
    public String getIp();
    public String getPort();
    public void join(Address address);
    public boolean isJoined(Address address);
    public Set<Map<String, String>> getNodes();
    public ConcurrentHashMap<String, String> getStorage();
    public void put(Data data);

}
