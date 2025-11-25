package com.siupo.restaurant;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class SiupoRestaurantApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiupoRestaurantApplication.class, args);
	}

}
