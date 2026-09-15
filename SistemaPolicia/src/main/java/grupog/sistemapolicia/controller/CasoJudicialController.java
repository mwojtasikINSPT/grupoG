package grupog.sistemapolicia.controller;

import grupog.sistemapolicia.dto.CasoJudicialRequest;
import grupog.sistemapolicia.model.Asalto;
import grupog.sistemapolicia.model.CasoJudicial;
import grupog.sistemapolicia.model.Juez;
import grupog.sistemapolicia.service.CasoJudicialService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/casos-judiciales")
public class CasoJudicialController {

    private final CasoJudicialService service;

    public CasoJudicialController(CasoJudicialService service) {
        this.service = service;
    }

    @GetMapping
    public List<CasoJudicial> listar() {
        return service.listar();
    }

    @GetMapping("/detenidos")
    public List<CasoJudicial> listarDetenidos() {
        return service.listarDetenidos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CasoJudicial guardar(@RequestBody CasoJudicialRequest datos) {
        Asalto asalto = new Asalto();
        asalto.setIdAsalto(datos.idAsalto());

        Juez juez = new Juez();
        juez.setClaveInterna(datos.claveJuez());

        CasoJudicial caso = new CasoJudicial(
                asalto, juez, datos.condenado(), datos.mesesCarcel());
        return service.guardar(caso);
    }
}
