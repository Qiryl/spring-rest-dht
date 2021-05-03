package spring.rest.dht;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DhtApplication {

	public static void main(String[] args) {
		var ctx = SpringApplication.run(DhtApplication.class, args);
	}

}
