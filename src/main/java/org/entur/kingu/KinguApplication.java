package org.entur.kingu;

import org.entur.pubsub.camel.config.GooglePubSubCamelComponentConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({GooglePubSubCamelComponentConfig.class})
public class KinguApplication {

	public static void main(String[] args) {
		SpringApplication.run(KinguApplication.class, args);
	}

}
