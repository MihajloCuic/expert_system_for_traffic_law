package com.ftn.sbnz.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Tells Spring Boot where to find JPA entities (model module) and Spring Data
 * repositories (service module).
 */
@SpringBootApplication
@EntityScan(basePackages = "com.ftn.sbnz.model")
@EnableJpaRepositories(basePackages = "com.ftn.sbnz.service.repository")
public class ServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}
}
