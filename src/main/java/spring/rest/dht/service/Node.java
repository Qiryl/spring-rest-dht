package spring.rest.dht.service;

import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import spring.rest.dht.model.Address;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Node {

    @Autowired
    private Address address;
    private Set<Address> joinedNodes = new HashSet<>();
    private ConcurrentHashMap<String, String> storage = new ConcurrentHashMap<>();

    @Autowired
    public Node(Address address) {
        this.address = address;
        joinNode(address);
    }

    public Address getAddress() {
        return address;
    }

    public Set<Address> getJoinedNodes() {
        return joinedNodes;
    }

    public void joinNode(Address address) {
        address.setId(sha1((address.getIp() + address.getPort()).getBytes()));
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

    public void setStorage(ConcurrentHashMap<String, String> storage) {
        this.storage = storage;
    }

    public String get(String key) {
        return storage.get(key);
    }

    public void removeValue(String key) {
        storage.remove(key);
    }

    public void put(MultipartFile file) throws IOException {
        storage.put(sha1(file.getOriginalFilename().getBytes()), file.getOriginalFilename());
        File destination = new File(
                System.getProperty("user.dir") +
                        "/storage/" +
                        address.getIp() + ":" + address.getPort() + "/" +
                        file.getOriginalFilename()
        );
        destination.mkdirs();
        file.transferTo(destination);
    }

    public static String sha1(byte[] input) {
        String sha1 = null;
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            byte[] result = mDigest.digest(input);
            sha1 = HexUtils.toHexString(result);
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        return sha1;
    }
}