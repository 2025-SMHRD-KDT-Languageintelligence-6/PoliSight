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


// 로컬
// http://localhost:8089/