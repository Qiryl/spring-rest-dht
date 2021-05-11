package spring.rest.dht.service;

import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import spring.rest.dht.model.Address;
import spring.rest.dht.model.Data;

import java.nio.ByteBuffer;
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
    public void joinNode(Address address) {
        if (node.notJoined(address)) {
            node.joinNode(address);

            String url = String.format("http://%s:%s/join", address.getIp(), address.getPort());
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Address> httpEntity = new HttpEntity<Address>(node.getAddress(), httpHeaders);
            restTemplate.postForObject(url, httpEntity, Void.class);
        }
    }

    @Override
    public Set<Address> getJoinedNodes() {
        return node.getJoinedNodes();
    }

    @Override
    public void putValue(Data data) {
        Address rn = responsibleNode(Node.sha1(data.getValue()));
        if (data.getId() == null) {
            data.setId(Node.sha1(data.getValue()));

            String url = String.format("http://%s:%s/put", rn.getIp(), rn.getPort());
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Data> httpEntity = new HttpEntity<Data>(data, httpHeaders);
            restTemplate.postForObject(url, httpEntity, Void.class);
        } else {
            node.putValue(data);
        }
    }

    @Override
    public String getValue(String key) {
        String value = node.getValue(key);
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
    public void delete() {
        String url;
        for (var joined : node.getJoinedNodes()) {
            url = String.format(
                    "http://%s:%s/remove?ip=%s&port=%s",
                    joined.getIp(), joined.getPort(),
                    node.getAddress().getIp(), node.getAddress().getPort()
            );
            restTemplate.delete(url);
        }

        var allValues = node.getAllValues();
        Address rn;
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Data> httpEntity;
        for (var entry : node.getAllValues().entrySet()) {
            rn = responsibleNode(entry.getKey());
            url = String.format("http://%s:%s/put", rn.getIp(), rn.getPort());
            httpEntity = new HttpEntity<Data>(new Data(entry.getKey(), entry.getValue()), httpHeaders);
            restTemplate.postForObject(url, httpEntity, Void.class);
        }
        SpringApplication.exit(context, () -> 0);
    }

    private Long weight(Address node, String key) {
        var bytes = HexUtils.fromHexString(Node.sha1(node.getIp() + node.getPort() + key));
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
