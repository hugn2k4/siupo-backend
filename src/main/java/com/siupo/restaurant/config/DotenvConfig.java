package com.siupo.restaurant.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {

    static {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();

            // Load all env vars from .env file to System properties  
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
            
            System.out.println("✅ Loaded " + dotenv.entries().size() + " variables from .env file");
        } catch (Exception e) {
            System.out.println("⚠️  .env file not found, using system environment variables");
        }
    }
}
