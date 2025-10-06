package com.Mini_Instagram_Demo.Mini_Instagram_Demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MiniInstagramDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniInstagramDemoApplication.class, args);
	}

}
