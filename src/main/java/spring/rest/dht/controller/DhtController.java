package spring.rest.dht.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import spring.rest.dht.model.Address;
import spring.rest.dht.model.Data;
import spring.rest.dht.service.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping
public class DhtController {

    @Autowired
    Node node;

    @GetMapping
    public String currentNode() {
        return node.getIp() + ":" + node.getPort() + " -- " + node.getId();
    }

    @PostMapping("/join")
    public void join(@RequestBody Address address) {
        if(!node.isJoined(address)) {
            node.join(address);

            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> data = new HashMap<String, String>();
            data.put("ip", node.getIp());
            data.put("port", node.getPort());
            HttpEntity<Map<String, String>> entity = new HttpEntity<Map<String, String>>(data, headers);

            restTemplate.postForObject("http://" + address.getIp() + ":" + address.getPort() + "/join", entity, Void.class);
        }
    }

    @GetMapping(value = "/node", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<Map<String, String>> joinedNodes() {
        return node.getNodes();
    }

    @GetMapping(value = "/tmp", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<Long> tmp() {
        return node.getTmp();
    }

    @PostMapping("/put")
    public void put(@RequestBody Data value) {
        var responsibleNode = node.responsibleNode(Node.sha1(value.getValue()));
        if (value.getId() == null) {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();

            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> data = new HashMap<String, String>();
            data.put("id", Node.sha1(value.getValue()));
            data.put("value", value.getValue());
            HttpEntity<Map<String, String>> entity = new HttpEntity<Map<String, String>>(data, headers);

            restTemplate.postForObject("http://" + responsibleNode.get("ip") + ":" + responsibleNode.get("port") + "/put", entity, Void.class);
        } else {
            node.put(value);
        }
    }

    @GetMapping("/storage/{key}")
    public String getValue(@PathVariable String key) {
        return node.getValue(key);
    }

    @GetMapping(value = "/storage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConcurrentHashMap<String, String> storage() {
        return node.getStorage();

    }

}
