package chauffeur.controller;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableAutoConfiguration
@RestController
public class About {
    @GetMapping("/about")
    public String about() {
        return "Chauffeur is a Java application for setting up mock REST API with Spring, set up as a learning experience.";
    }
}
