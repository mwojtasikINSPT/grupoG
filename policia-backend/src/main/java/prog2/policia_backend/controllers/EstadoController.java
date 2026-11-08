package prog2.policia_backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//responderá OK cuando el backend esté disponible
@RestController
public class EstadoController {

    @GetMapping("/api/estado")
    public String consultarEstado() {
        return "OK";
    }
}