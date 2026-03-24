package chauffeur.controller;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "chauffeur.controller", "chauffeur.radio" })
public class Controller {
}
