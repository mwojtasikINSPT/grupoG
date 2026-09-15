
package grupog.sistemapolicia.controller;

import grupog.sistemapolicia.model.Vigilante;
import grupog.sistemapolicia.service.VigilanteService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vigilantes")
public class VigilanteController {

    private final VigilanteService service;

    public VigilanteController(VigilanteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Vigilante> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vigilante guardar(
            @RequestBody Vigilante vigilante) {

        return service.guardar(vigilante);
    }
}