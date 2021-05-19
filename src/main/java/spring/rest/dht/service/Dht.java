package spring.rest.dht.service;

import org.springframework.web.multipart.MultipartFile;
import spring.rest.dht.model.Address;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface Dht {

    void joinNode(Address address) throws IOException;
    Set<Address> getJoinedNodes();
    void distribute(Address address) throws IOException;
    void putValue(MultipartFile file) throws IOException;
    void putDirect(MultipartFile file) throws IOException;
    String getValue(String key);
    ConcurrentHashMap<String, String> getAllValues();
    void removeJoinedNode(Address address);
    void delete() throws IOException;

}
