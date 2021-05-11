package spring.rest.dht.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import spring.rest.dht.model.Address;
import spring.rest.dht.model.Data;
import spring.rest.dht.service.Dht;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping
public class DhtController {

    @Autowired
    private ApplicationContext context;

    @Autowired
    @Resource(name="${dht.type}")
    private Dht dht;

    @GetMapping("/")
    public String currentNode() {
        return "";
    }

    @PostMapping("/join")
    public void join(@RequestBody Address address) {
        dht.joinNode(address);

        // if(!node.isJoined(address)) {
        //     node.join(address);
        //     var storage = node.getStorage();

        //     HttpHeaders headers = new HttpHeaders();
        //     RestTemplate restTemplate = new RestTemplate();
        //     Data data = new Data();
        //     Map<String, String> responsibleNode;
        //     HttpEntity<Data> entity;
        //     String url;

        //     headers.setContentType(MediaType.APPLICATION_JSON);

        //     for (var entry : storage.entrySet()) {
        //         responsibleNode = node.responsibleNode(entry.getValue());
        //         url = "http://" + responsibleNode.get("ip") + ":" + responsibleNode.get("port") + "/put";

        //         data.setId(entry.getKey());
        //         data.setValue(entry.getValue());

        //         entity = new HttpEntity<Data>(data, headers);
        //         node.deleteValue(entry.getKey());
        //         restTemplate.postForObject(url, entity, Void.class);
        //     }

        //     Map<String, String> addr = new HashMap<String, String>();
        //     addr.put("ip", node.getIp());
        //     addr.put("port", node.getPort());
        //     HttpEntity<Map<String, String>> httpEntity = new HttpEntity<Map<String, String>>(addr, headers);

        //     restTemplate.postForObject("http://" + address.getIp() + ":" + address.getPort() + "/join", httpEntity, Void.class);
        // }
    }

    @GetMapping(value = "/node", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<Address> getJoinedNodes() {
        return dht.getJoinedNodes();
    }

    @PostMapping("/put")
    public void put(@RequestBody Data data) {
        dht.putValue(data);
    }

    @GetMapping(value = "/storage/{key}")
    public String getValue(@PathVariable String key) {
        return dht.getValue(key);
   }

    @GetMapping(value = "/storage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConcurrentHashMap<String, String> getAllValues() {
        return dht.getAllValues();
    }

    // curl -X DELETE "http://localhost:8081/delete?ip=localhost&port=8080"
    @DeleteMapping("/remove")
    public void removeJoinedNode(@RequestParam("ip") String ip, @RequestParam("port") String port) {
        dht.removeJoinedNode(new Address(ip, port));
    }

    @DeleteMapping("/")
    public void delete() {
        dht.delete();
    }

}
