package br.fitforge.treinos.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** A raiz do Web App abre a documentação Swagger. */
@Hidden
@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "redirect:/swagger-ui.html";
    }
}
