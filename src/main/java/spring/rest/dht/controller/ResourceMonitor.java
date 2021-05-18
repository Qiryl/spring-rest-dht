package spring.rest.dht.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/monitor")
public class ResourceMonitor {

    @Value("${server.address}")
    String address;
    @Value("${server.port}")
    String port;

    RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/memory")
    public Map<String, String> getMemoryUsage() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        Map<String, String> memory = new HashMap<>() {{
            put("used", humanReadableByteCountSI(memoryMXBean.getHeapMemoryUsage().getUsed()));
            put("max", humanReadableByteCountSI(memoryMXBean.getHeapMemoryUsage().getMax()));
        }};
        return memory;
    }

    @GetMapping("/disk")
    public Map<String, String> getDiskUsage() throws IOException {
        Path folder = Paths.get(System.getProperty("user.dir") + "/storage/" + address + ":" + port);
        long size = Files.walk(folder)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();

        Map<String, String> disk = new HashMap<>() {{
            put("used", humanReadableByteCountSI(size));
            put("total", humanReadableByteCountSI(folder.toFile().getTotalSpace()));
            put("free", humanReadableByteCountSI(folder.toFile().getFreeSpace()));
        }};
        return disk;
    }

    @GetMapping("/amount")
    public String getNumberOfRecords() {
        String url = String.format("http://%s:%s/storage", address, port);
        var storage = restTemplate.getForObject(url, ConcurrentHashMap.class);
        return String.valueOf(storage.size());
    }

    private String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

}
