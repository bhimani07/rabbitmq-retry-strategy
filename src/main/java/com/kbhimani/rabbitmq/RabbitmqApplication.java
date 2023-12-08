package com.kbhimani.rabbitmq;

import com.kbhimani.rabbitmq.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class RabbitmqApplication {

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqApplication.class, args);
	}

}
