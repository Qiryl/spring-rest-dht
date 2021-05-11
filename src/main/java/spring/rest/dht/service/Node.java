package spring.rest.dht.service;

import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import spring.rest.dht.model.Address;
import spring.rest.dht.model.Data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Node {

    @Autowired
    private Address address;
    private Set<Address> joinedNodes = new HashSet<Address>();
    private ConcurrentHashMap<String, String> storage = new ConcurrentHashMap<String, String>();

    public Address getAddress() {
        return address;
    }

    public Set<Address> getJoinedNodes() {
        return joinedNodes;
    }

    public void joinNode(Address address) {
        address.setId(sha1(address.getIp() + address.getPort()));
        joinedNodes.add(address);
    }

    public boolean notJoined(Address address) {
        return joinedNodes.stream().filter(current ->
                current.getIp().equals(address.getIp()) && current.getPort().equals(address.getPort()))
                .findFirst()
                .isEmpty();
    }

    public void removeJoinedNode(Address address) {
        joinedNodes.removeIf(current ->
                current.getIp().equals(address.getIp()) && current.getPort().equals(address.getPort()));
    }

    public ConcurrentHashMap<String, String> getAllValues() {
        return storage;
    }

    public String getValue(String key) {
        return storage.get(key);
    }

    public void removeValue(String key) {
        storage.remove(key);
    }

    public void putValue(Data data) {
        if (data.getId() != null) {
            storage.put(data.getId(), data.getValue());
        } else {
            storage.put(sha1(data.getValue()), data.getValue());
        }
    }

    public static String sha1(String input) {
        String sha1 = null;
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            byte[] result = mDigest.digest(input.getBytes());
            sha1 = HexUtils.toHexString(result);
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        return sha1;
    }

}
