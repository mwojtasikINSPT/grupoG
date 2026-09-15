package grupog.sistemapolicia.controller;

import grupog.sistemapolicia.model.EntidadBancaria;
import grupog.sistemapolicia.service.EntidadBancariaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/entidades-bancarias")
public class EntidadBancariaController {

    private final EntidadBancariaService service;

    public EntidadBancariaController(
            EntidadBancariaService service) {

        this.service = service;
    }

    @GetMapping
    public List<EntidadBancaria> listar() {
        return service.listar();
    }

    @PostMapping
    public EntidadBancaria guardar(
            @RequestBody EntidadBancaria entidad) {

        return service.guardar(entidad);
    }
}