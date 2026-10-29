package br.com.zeladoria.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Hidden
public class DocsRedirectController {

    @GetMapping("/docs")
    public String redirectToDocs() {
        return "redirect:/docs.html";
    }
}
