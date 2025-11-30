package com.cadify.cadifyWAS;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@EnableAsync
public class CadifyWasApplication {

	public static void main(String[] args) {
		SpringApplication.run(CadifyWasApplication.class, args);



	}

}