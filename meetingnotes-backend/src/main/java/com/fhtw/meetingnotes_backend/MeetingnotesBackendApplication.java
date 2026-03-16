package com.fhtw.meetingnotes_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MeetingnotesBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetingnotesBackendApplication.class, args);
	}

}
