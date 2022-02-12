package com.hkjc.test.solacetest;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import com.hkjc.test.solacetest.service.TransactionService;
import com.solacesystems.jcsmp.EndpointProperties;

@SpringBootApplication
public class SolaceTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolaceTestApplication.class, args);
	}

}
