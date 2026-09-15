package grupog.sistemapolicia.controller;

import grupog.sistemapolicia.dto.ContratoVigilanciaRequest;
import grupog.sistemapolicia.model.ContratoVigilancia;
import grupog.sistemapolicia.model.Sucursal;
import grupog.sistemapolicia.model.Vigilante;
import grupog.sistemapolicia.service.ContratoVigilanciaService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contratos-vigilancia")
public class ContratoVigilanciaController {

    private final ContratoVigilanciaService service;

    public ContratoVigilanciaController(
            ContratoVigilanciaService service) {

        this.service = service;
    }

    @GetMapping
    public List<ContratoVigilancia> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoVigilancia guardar(
            @RequestBody ContratoVigilanciaRequest datos) {

        Sucursal sucursal = new Sucursal();
        sucursal.setCodigo(datos.codigoSucursal());

        Vigilante vigilante = new Vigilante();
        vigilante.setCodigo(datos.codigoVigilante());

        ContratoVigilancia contrato =
                new ContratoVigilancia(
                    datos.codigo(),
                    sucursal,
                    vigilante,
                    datos.fecha(),
                    datos.conArma()
                );

        return service.guardar(contrato);
    }
}
