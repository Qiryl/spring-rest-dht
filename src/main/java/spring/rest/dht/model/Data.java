package spring.rest.dht.model;

import org.springframework.core.io.Resource;
import java.io.Serializable;

public class Data implements Serializable {
    private String key;
    private Resource value;

    public Data(String key, Resource value) {
        this.key = key;
        this.value = value;
    }

    public String getId() {
        return key;
    }

    public void setId(String key) {
        this.key = key;
    }

    public Resource getValue() {
        return value;
    }

    public void setValue(Resource value) {
        this.value = value;
    }

}