package org.entur.kingu;

import org.entur.pubsub.base.config.GooglePubSubConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({GooglePubSubConfig.class})
public class KinguApplication {

	public static void main(String[] args) {
		SpringApplication.run(KinguApplication.class, args);
	}

}
