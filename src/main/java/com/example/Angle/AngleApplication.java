package com.example.Angle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync(proxyTargetClass=true)
public class AngleApplication {

	public static void main(String[] args) {
		SpringApplication.run(AngleApplication.class,args);
	}

}
