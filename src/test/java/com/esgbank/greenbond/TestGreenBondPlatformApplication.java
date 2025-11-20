package com.esgbank.greenbond;

import org.springframework.boot.SpringApplication;

public class TestGreenBondPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.from(GreenBondPlatformApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
