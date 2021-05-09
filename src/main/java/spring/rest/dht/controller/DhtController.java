package spring.rest.dht.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
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
    private Node node;

    @Autowired
    private ApplicationContext context;

    @GetMapping
    public String currentNode() {
        return node.getIp() + ":" + node.getPort() + " -- " + node.getId();
    }

    @PostMapping("/join")
    public void join(@RequestBody Address address) {
        if(!node.isJoined(address)) {
            node.join(address);
            var storage = node.getStorage();

            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            Data data = new Data();
            Map<String, String> responsibleNode;
            HttpEntity<Data> entity;
            String url;

            headers.setContentType(MediaType.APPLICATION_JSON);

            for (var entry : storage.entrySet()) {
                responsibleNode = node.responsibleNode(entry.getValue());
                url = "http://" + responsibleNode.get("ip") + ":" + responsibleNode.get("port") + "/put";

                data.setId(entry.getKey());
                data.setValue(entry.getValue());

                entity = new HttpEntity<Data>(data, headers);
                node.deleteValue(entry.getKey());
                restTemplate.postForObject(url, entity, Void.class);
                System.out.println(entry + " -- " + responsibleNode);
            }

            Map<String, String> addr = new HashMap<String, String>();
            addr.put("ip", node.getIp());
            addr.put("port", node.getPort());
            HttpEntity<Map<String, String>> httpEntity = new HttpEntity<Map<String, String>>(addr, headers);

            restTemplate.postForObject("http://" + address.getIp() + ":" + address.getPort() + "/join", httpEntity, Void.class);
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

    @GetMapping(value = "/storage/{key}")
    public String getValue(@PathVariable String key) {
        String value = node.getValue(key);
        if (value != null) {
            return value;
        } else {
            var resposibleNode = node.responsibleNode(key);
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://" + resposibleNode.get("ip") + ":" + resposibleNode.get("port") + "/storage/" + key;
            String result = restTemplate.getForObject(url, String.class);
            return result;
        }
    }

    @GetMapping(value = "/storage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConcurrentHashMap<String, String> storage() {
        return node.getStorage();
    }

    // curl -X DELETE "http://localhost:8081/delete?ip=localhost&port=8080"
    @DeleteMapping("/delete")
    public void delete(@RequestParam("ip") String ip, @RequestParam("port") String port) {
        Address address = new Address();
        address.setIp(ip);
        address.setPort(port);
        node.deleteNode(address);
    }

    @DeleteMapping("/")
    public void deleteCurrentNode() {
        var storage = node.getStorage();

        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        Data data = new Data();
        Map<String, String> responsibleNode;
        HttpEntity<Data> entity;
        String url;

        for (var joined : node.getNodes()) {
            url = "http://" + joined.get("ip") + ":" +joined.get("port") + "/delete?ip=" + node.getIp() + "&port=" + node.getPort();
            restTemplate.delete(url);
        }

        headers.setContentType(MediaType.APPLICATION_JSON);
        for (var entry : storage.entrySet()) {
            responsibleNode = node.responsibleNode(entry.getKey());
            url = "http://" + responsibleNode.get("ip") + ":" + responsibleNode.get("port") + "/put";

            data.setId(entry.getKey());
            data.setValue(entry.getValue());

            entity = new HttpEntity<Data>(data, headers);
            restTemplate.postForObject(url, entity, Void.class);
        }
        SpringApplication.exit(context, () -> 0);
    }

}
