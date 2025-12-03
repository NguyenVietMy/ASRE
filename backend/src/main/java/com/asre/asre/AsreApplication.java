package com.asre.asre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AsreApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsreApplication.class, args);
	}

}
