package com.github.jnaalisv.sf_fds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.InstantSource;

@SpringBootApplication
public class SfFdsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SfFdsApplication.class, args);
	}

	@Bean
	public InstantSource instantSource() {
		return Clock.systemUTC();
	}

}
