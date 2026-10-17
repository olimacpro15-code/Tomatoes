package com.tomatoes.insumos;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

import java.util.TimeZone;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class InsumosApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsumosApplication.class, args);
    }

    /** Todas las fechas del sistema se manejan en hora de Colombia. */
    @PostConstruct
    void zonaHoraria() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
    }
}
