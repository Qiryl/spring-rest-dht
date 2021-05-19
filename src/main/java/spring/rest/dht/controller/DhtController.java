package spring.rest.dht.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring.rest.dht.model.Address;
import spring.rest.dht.service.Dht;

import javax.annotation.Resource;
import java.io.IOException;
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
        return "hello";
    }

    @PostMapping("/join")
    public void join(@RequestBody Address address) throws IOException {
        dht.joinNode(address);
    }

    @GetMapping(value = "/node", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<Address> getJoinedNodes() {
        return dht.getJoinedNodes();
    }

    @PostMapping(value = "/put")
    public void put(@RequestParam("file") MultipartFile file) throws IOException {
        dht.putValue(file);
    }

    @PostMapping(value = "/put/direct")
    public void putDirect(@RequestParam("file") MultipartFile file) throws Exception {
        dht.putDirect(file);
    }

    @GetMapping(value = "/storage/{key}")
    public String getValue(@PathVariable String key) {
        return dht.getValue(key);
   }

    @GetMapping(value = "/storage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ConcurrentHashMap<String, String> getAllValues() {
        return dht.getAllValues();
    }

    @PostMapping(value = "/storage/distribute")
    public void distribute(@RequestBody Address address) throws IOException {
        dht.distribute(address);
    }

    // curl -X DELETE "http://localhost:8081/remove?ip=localhost&port=8080"
    @DeleteMapping("/node/remove")
    public void removeJoinedNode(@RequestParam("ip") String ip, @RequestParam("port") String port) {
        dht.removeJoinedNode(new Address(ip, port));
    }

    @DeleteMapping("/")
    public void delete() throws IOException {
        dht.delete();
    }

}