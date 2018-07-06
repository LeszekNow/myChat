package com.leszeknowinski.myChat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
public class MyChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyChatApplication.class, args);
	}
}
