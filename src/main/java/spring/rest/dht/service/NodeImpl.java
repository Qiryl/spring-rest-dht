package spring.rest.dht.service;

import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import spring.rest.dht.model.Address;
import spring.rest.dht.model.Data;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NodeImpl implements Node {

    private String id;
    private String ip;
    private String port;

    private Set<Map<String, String>> nodes = new HashSet<Map<String, String>>();
    private Set<Long> tmp = new TreeSet<Long>();
    private ConcurrentHashMap<String, String> storage = new ConcurrentHashMap<String, String>();

    public NodeImpl(@Value("${server.address}") String ip, @Value("${server.port}") String port) {
        this.id = sha1(ip + port).toString();
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
    public Set<Long> getTmp() {
        return tmp;
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
    public String getValue(String key) {
        return storage.get(key);
    }

    private static Long weight(Map<String, String> node, String key) {
        var bytes = HexUtils.fromHexString(sha1(node.get("ip") + node.get("port") + key));
        return ByteBuffer.wrap(bytes).getLong();
    }

    @Override
    public Map<String, String> responsibleNode(String key) {
        long weight;
        long maxWeight = 0;
        var candidate = nodes.iterator().next();

        for (var node : nodes) {
            weight = weight(node, key);
            if (weight > maxWeight) {
                maxWeight = weight;
                candidate = node;
            }
        }

        return candidate;
    }

    @Override
    public void join(Address address) {
        var node = new HashMap<String, String>();
        node.put("id", sha1(address.getIp() + address.getPort()).toString());
        node.put("ip", address.getIp());
        node.put("port", address.getPort());
        nodes.add(node);
        var bytes = HexUtils.fromHexString(sha1(address.getIp() + address.getPort()));
        tmp.add(ByteBuffer.wrap(bytes).getLong());
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
