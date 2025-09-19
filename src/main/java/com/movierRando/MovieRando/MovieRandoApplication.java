package com.movierRando.MovieRando;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableCaching
@EnableRetry
public class MovieRandoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovieRandoApplication.class, args);
	}

}
