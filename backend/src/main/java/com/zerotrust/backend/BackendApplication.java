package com.zerotrust.backend;

import com.zerotrust.backend.services.DataLoaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class BackendApplication {

    private final DataLoaderService dataLoaderService;

    public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
