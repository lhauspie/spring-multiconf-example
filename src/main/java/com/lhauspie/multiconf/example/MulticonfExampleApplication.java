package com.lhauspie.multiconf.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MulticonfExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(MulticonfExampleApplication.class, args);
	}

}
