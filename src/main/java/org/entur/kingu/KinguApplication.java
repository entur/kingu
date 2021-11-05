package org.entur.kingu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("org.entur.kingu.config")
public class KinguApplication {

	public static void main(String[] args) {
		SpringApplication.run(KinguApplication.class, args);
	}

}
