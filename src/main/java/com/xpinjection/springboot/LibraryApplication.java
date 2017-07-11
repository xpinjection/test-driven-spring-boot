package com.xpinjection.springboot;

import com.xpinjection.springboot.init.InventorySettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(InventorySettings.class)
public class LibraryApplication {
	public static void main(String[] args) {
		new SpringApplication(LibraryApplication.class).run(args);
	}
}
