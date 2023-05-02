package org.entur.kingu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ComponentScan(excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = KinguApplication.class),
})
public class TestApp extends KinguApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApp.class, args);
    }
}
