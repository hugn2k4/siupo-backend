package com.siupo.restaurant;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SiupoRestaurantApplication {

	static {
		// Load .env file before Spring Boot starts
		try {
			Dotenv dotenv = Dotenv.configure()
					.directory("./")
					.ignoreIfMissing()
					.load();

			// Set all env vars to System properties
			dotenv.entries().forEach(entry -> {
				System.setProperty(entry.getKey(), entry.getValue());
			});

			System.out.println("✅ Loaded " + dotenv.entries().size() + " variables from .env file");
		} catch (Exception e) {
			System.out.println("⚠️ .env file not found, using system environment variables");
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(SiupoRestaurantApplication.class, args);
	}

}
