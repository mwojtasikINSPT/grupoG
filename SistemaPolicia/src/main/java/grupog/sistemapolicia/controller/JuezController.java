package grupog.sistemapolicia.controller;

import grupog.sistemapolicia.model.Juez;
import grupog.sistemapolicia.service.JuezService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jueces")
public class JuezController {

    private final JuezService service;

    public JuezController(JuezService service) {
        this.service = service;
    }

    @GetMapping
    public List<Juez> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Juez guardar(@RequestBody Juez juez) {
        return service.guardar(juez);
    }
}
