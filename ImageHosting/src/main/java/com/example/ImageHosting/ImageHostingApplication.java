package com.example.ImageHosting;

import com.example.ImageHosting.property.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class
})
public class ImageHostingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageHostingApplication.class, args);
	}

}
