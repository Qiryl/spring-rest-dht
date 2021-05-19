package spring.rest.dht.service;

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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Qualifier("replication")
public class Replication implements Dht {

    @Autowired
    private Node node;

    @Autowired
    private ApplicationContext context;

    private RestTemplate restTemplate = new RestTemplate();
    private HttpHeaders httpHeaders = new HttpHeaders();

    @Override
    public void joinNode(Address address) {
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
    public void distribute(Address address) {
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, Object>> httpEntity;
        String url;
        for (var entry : node.getAllValues().entrySet()) {
            File file = new File(
                    System.getProperty("user.dir") +
                            "/storage/" +
                            node.getAddress().getIp() + ":" + node.getAddress().getPort() + "/" +
                            entry.getValue()
            );
            Resource resource = new FileSystemResource(file);
            url = String.format("http://%s:%s/put/direct", address.getIp(), address.getPort());
            data.clear();
            data.add("file", resource);
            httpEntity = new HttpEntity<>(data, httpHeaders);
            restTemplate.postForObject(url, httpEntity, Void.class);
        }
    }


    @Override
    public Set<Address> getJoinedNodes() {
        return node.getJoinedNodes();
    }

    @Override
    public void putValue(MultipartFile file) throws IOException {
        String url;
        HttpEntity<MultiValueMap<String, Object>> httpEntity;
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        for (var joined : node.getJoinedNodes()) {
            url = String.format("http://%s:%s/put/direct", joined.getIp(), joined.getPort());
            httpEntity = new HttpEntity<>(body, httpHeaders);
            restTemplate.postForObject(url, httpEntity, Void.class);
        }
    }

    @Override
    public void putDirect(MultipartFile file) throws IOException {
        node.put(file);
    }

    @Override
    public String getValue(String key) {
        return node.get(key);
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
    public void delete() {
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
        SpringApplication.exit(context, () -> 0);
    }

}