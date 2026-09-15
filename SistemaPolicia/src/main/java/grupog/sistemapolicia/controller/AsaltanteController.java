package grupog.sistemapolicia.controller;

import grupog.sistemapolicia.dto.AsaltanteRequest;
import grupog.sistemapolicia.model.Asaltante;
import grupog.sistemapolicia.model.Banda;
import grupog.sistemapolicia.service.AsaltanteService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/asaltantes")
public class AsaltanteController {

    private final AsaltanteService service;

    public AsaltanteController(AsaltanteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Asaltante> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Asaltante guardar(
            @RequestBody AsaltanteRequest datos) {

        Banda banda = new Banda();
        banda.setNumeroBanda(datos.numeroBanda());

        Asaltante asaltante = new Asaltante(
                datos.clave(),
                datos.nombreCompleto(),
                banda
        );

        return service.guardar(asaltante);
    }
}
