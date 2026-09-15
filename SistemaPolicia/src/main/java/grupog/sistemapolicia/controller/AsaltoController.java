package grupog.sistemapolicia.controller;

import grupog.sistemapolicia.dto.AsaltoRequest;
import grupog.sistemapolicia.model.Asaltante;
import grupog.sistemapolicia.model.Asalto;
import grupog.sistemapolicia.model.Sucursal;
import grupog.sistemapolicia.service.AsaltoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/asaltos")
public class AsaltoController {

    private final AsaltoService service;

    public AsaltoController(AsaltoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Asalto> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Asalto guardar(@RequestBody AsaltoRequest datos) {
        Asaltante asaltante = new Asaltante();
        asaltante.setClave(datos.claveAsaltante());

        Sucursal sucursal = new Sucursal();
        sucursal.setCodigo(datos.codigoSucursal());

        Asalto asalto = new Asalto(
                datos.idAsalto(), asaltante, sucursal, datos.fecha());
        return service.guardar(asalto);
    }
}
