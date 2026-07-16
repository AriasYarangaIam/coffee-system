package com.coffee.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

// Punto de arranque de la aplicacion Spring Boot.
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		// El sistema opera en Perú: fijamos la zona por defecto del JVM ANTES de arrancar
		// para que todo LocalDateTime.now()/@CurrentTimestamp genere hora America/Lima (UTC-5).
		TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
		SpringApplication.run(BackendApplication.class, args);
	}

}
