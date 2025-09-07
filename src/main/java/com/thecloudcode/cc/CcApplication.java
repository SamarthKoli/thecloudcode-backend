package com.thecloudcode.cc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CcApplication {

	public static void main(String[] args) {
		SpringApplication.run(CcApplication.class, args);
	}

}
