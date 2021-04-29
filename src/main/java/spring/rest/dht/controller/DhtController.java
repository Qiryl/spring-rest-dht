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
        return node.getIp() + ":" + node.getPort();
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

    // curl -H "Content-Type: application/json" -X POST -d '{"id":"1", "value":"value on 8080"}' http://localhost:8080/put
    @PostMapping("/put")
    public void put(@RequestBody Data value) {
        System.out.println("putting value");
        node.put(value);
    }

    @GetMapping(value = "/storage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConcurrentHashMap<String, String> storage() {
        return node.getStorage();
    }

}
