package spring.rest.dht.service;

import spring.rest.dht.model.Address;

import java.util.Map;
import java.util.Set;

public interface Node {

    public String getId();
    public String getIp();
    public String getPort();
    public void join(Address address);
    public boolean isJoined(Address address);
    public Set<Map<String, String>> getNodes();

}
