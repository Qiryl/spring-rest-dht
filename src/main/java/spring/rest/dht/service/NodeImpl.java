package spring.rest.dht.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import spring.rest.dht.model.Address;
import spring.rest.dht.model.Data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NodeImpl implements Node {

    private String id;
    private String ip;
    private String port;

    private Set<Map<String, String>> nodes = new HashSet<Map<String, String>>();
    private ConcurrentHashMap<String, String> storage = new ConcurrentHashMap<String, String>();

    public NodeImpl(@Value("${server.address}") String ip, @Value("${server.port}") String port) {
        this.id = sha1(ip + port);
        this.ip = ip;
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    @Override
    public Set<Map<String, String>> getNodes() {
        return nodes;
    }

    @Override
    public ConcurrentHashMap<String, String> getStorage() {
        return storage;
    }

    @Override
    public void put(Data value) {
        storage.put(sha1(value.getValue()), value.getValue());
    }

    @Override
    public void join(Address address) {
        var node = new HashMap<String, String>();
        node.put("id", sha1(address.getIp() + address.getPort()));
        node.put("ip", address.getIp());
        node.put("port", address.getPort());
        nodes.add(node);
    }

    @Override
    public boolean isJoined(Address address) {
        var node = nodes.stream()
                .filter(n -> n.get("ip").equals(address.getIp()) && n.get("port").equals(address.getPort()))
                .findFirst();
        if (node.isEmpty()) {
            return false;
        }
        return true;
    }

    private static String sha1(String input) {
        String sha1 = null;
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            byte[] result = mDigest.digest(input.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < result.length; i++) {
                sb.append(Integer.toString(result[i], 2).substring(1));
            }
            sha1 = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sha1;
    }

}
