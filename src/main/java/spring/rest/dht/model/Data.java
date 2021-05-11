package spring.rest.dht.model;

public class Data {
    private String key;
    private String value;

    public Data() { }

    public Data(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getId() {
        return key;
    }

    public void setId(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
