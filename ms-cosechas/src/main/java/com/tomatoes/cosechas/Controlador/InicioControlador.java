package com.tomatoes.cosechas.Controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import io.swagger.v3.oas.annotations.Hidden;

@Controller
@Hidden
public class InicioControlador {

    @GetMapping("/")
    public String raiz() {
        return "redirect:/swagger-ui.html";
    }
}
