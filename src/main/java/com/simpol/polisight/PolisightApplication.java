package com.simpol.polisight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PolisightApplication {

	public static void main(String[] args) {
		SpringApplication.run(PolisightApplication.class, args);
	}

}

// 클라우드 (GCP)
// http://34.50.52.105.nip.io:8089/
// 로컬
// http://localhost:8089/