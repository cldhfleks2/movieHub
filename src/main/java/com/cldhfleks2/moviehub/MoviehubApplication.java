package com.cldhfleks2.moviehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling //DailyTask에서 스케쥴러 사용하기위함
public class MoviehubApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoviehubApplication.class, args);
	}

}
