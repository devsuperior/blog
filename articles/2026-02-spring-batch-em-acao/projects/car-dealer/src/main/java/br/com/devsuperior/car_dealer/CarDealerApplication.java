package br.com.devsuperior.car_dealer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CarDealerApplication {

	// Inicializa o Spring Boot e dispara o job configurado.
	public static void main(String[] args) {
		SpringApplication.run(CarDealerApplication.class, args);
	}

}
