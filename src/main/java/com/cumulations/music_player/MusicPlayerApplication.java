package com.cumulations.music_player;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.cumulations.music_player.config.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = {
		FileStorageProperties.class
})
public class MusicPlayerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MusicPlayerApplication.class, args);
	}


}
