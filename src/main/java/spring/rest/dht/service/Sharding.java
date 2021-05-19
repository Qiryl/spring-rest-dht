package spring.rest.dht.service;

import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import spring.rest.dht.model.Address;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Qualifier("sharding")
public class Sharding implements Dht {

    @Autowired
    private Node node;

    @Autowired
    private ApplicationContext context;

    private RestTemplate restTemplate = new RestTemplate();
    private HttpHeaders httpHeaders = new HttpHeaders();

    @Override
    public void joinNode(Address address) throws IOException {
        if (node.notJoined(address)) {
            node.joinNode(address);

            String url = String.format("http://%s:%s/join", address.getIp(), address.getPort());
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Address> httpEntity = new HttpEntity<>(node.getAddress(), httpHeaders);
            restTemplate.postForObject(url, httpEntity, Void.class);
        } else {
            if (node.getAllValues().isEmpty()) {
                String url = String.format("http://%s:%s/storage/distribute", address.getIp(), address.getPort());
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Address> httpEntity = new HttpEntity<>(node.getAddress(), httpHeaders);
                restTemplate.postForObject(url, httpEntity, Void.class);
            } else {
                distribute(address);
            }
        }
    }

    @Override
    public void distribute(Address address) throws IOException {
        Address rn;
        String url;
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, Object>> httpEntity;

        for (var entry : node.getAllValues().entrySet()) {
            File file = new File(
                    System.getProperty("user.dir") +
                            "/storage/" +
                            node.getAddress().getIp() + ":" + node.getAddress().getPort() + "/" +
                            entry.getValue()
            );
            rn = responsibleNode(Node.sha1(Files.readAllBytes(file.toPath())));
            if (rn == node.getAddress())
                continue;
            Resource resource = new FileSystemResource(file);
            url = String.format("http://%s:%s/put/direct", rn.getIp(), rn.getPort());
            data.clear();
            data.add("file", resource);
            httpEntity = new HttpEntity<>(data, httpHeaders);
            restTemplate.postForObject(url, httpEntity, Void.class);
            file.delete();
            node.removeValue(Node.sha1(entry.getValue().getBytes()));
        }
    }

    private void removeValue() {

    }

    @Override
    public Set<Address> getJoinedNodes() {
        return node.getJoinedNodes();
    }

    @Override
    public void putValue(MultipartFile file) throws IOException {
        Address rn = responsibleNode(Node.sha1(file.getBytes()));

        String url = String.format("http://%s:%s/put/direct", rn.getIp(), rn.getPort());
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
        data.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(data, httpHeaders);
        restTemplate.postForEntity(url, httpEntity, Void.class);
    }

    @Override
    public void putDirect(MultipartFile file) throws IOException {
        node.put(file);
    }

    @Override
    public String getValue(String key) {
        String value = node.get(key);
        if (value != null) {
            return value;
        } else {
            Address rn = responsibleNode(key);
            String url = "http://" + rn.getIp() + ":" + rn.getPort() + "/storage/" + key;
            String result = restTemplate.getForObject(url, String.class);
            return result;
        }
    }

    @Override
    public ConcurrentHashMap<String, String> getAllValues() {
        return node.getAllValues();
    }

    @Override
    public void removeJoinedNode(Address address) {
        node.removeJoinedNode(address);
    }

    @Override
    public void delete() throws IOException {
        String url;
        Address address = node.getAddress();
        removeJoinedNode(address);
        for (var joined : node.getJoinedNodes()) {
            url = String.format(
                    "http://%s:%s/node/remove?ip=%s&port=%s",
                    joined.getIp(), joined.getPort(),
                    address.getIp(), address.getPort()
            );
            restTemplate.delete(url);
        }

        Address rn;
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, Object>> httpEntity;
        for (var entry : node.getAllValues().entrySet()) {
            File file = new File(
                    System.getProperty("user.dir") +
                            "/storage/" +
                            address.getIp() + ":" + address.getPort() + "/" +
                            entry.getValue()
            );
            Resource resource = new FileSystemResource(file);
            rn = responsibleNode(Node.sha1(Files.readAllBytes(file.toPath())));
            url = String.format("http://%s:%s/put/direct", rn.getIp(), rn.getPort());
            data.add("file", resource);
            httpEntity = new HttpEntity<>(data, httpHeaders);
            restTemplate.postForObject(url, httpEntity, Void.class);
        }
        SpringApplication.exit(context, () -> 0);
    }

    private Long weight(Address node, String key) {
        var bytes = HexUtils.fromHexString(Node.sha1((node.getIp() + node.getPort() + key).getBytes()));
        return ByteBuffer.wrap(bytes).getLong();
    }

    public Address responsibleNode(String key) {
        long weight;
        long maxWeight = 0;
        var joinedNodes = node.getJoinedNodes();
        var candidate = joinedNodes.iterator().next();

        for (var node : joinedNodes) {
            weight = weight(node, key);
            if (weight > maxWeight) {
                maxWeight = weight;
                candidate = node;
            }
        }
        return candidate;
    }

}