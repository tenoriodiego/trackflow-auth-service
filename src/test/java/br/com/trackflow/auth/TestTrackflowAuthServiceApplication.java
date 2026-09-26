package br.com.trackflow.auth;

import org.springframework.boot.SpringApplication;

public class TestTrackflowAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(TrackflowAuthServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
