package spring.rest.dht.service;

import org.apache.tomcat.util.buf.HexUtils;
import spring.rest.dht.model.Address;
import spring.rest.dht.model.Data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    public String getValue(String key);

    public void put(Data data);
    public Map<String, String> responsibleNode(String key);
    public Set<Long> getTmp();

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
