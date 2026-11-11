package com.github.jnaalisv.sf_fds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.InstantSource;

// No user accounts: callers authenticate with API keys (see http.security).
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class SfFdsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SfFdsApplication.class, args);
	}

	@Bean
	public InstantSource instantSource() {
		return Clock.systemUTC();
	}

}
