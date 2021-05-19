package spring.rest.dht.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import spring.rest.dht.service.Node;

@Component
public class Address {

    private String id;
    private String ip;
    private String port;

    public Address(@Value("${server.address}") String ip, @Value("${server.port}") String port) {
        this.id = Node.sha1((ip + port).getBytes());
        this.ip = ip;
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

}
