package prog2.policia_backend.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prog2.policia_backend.DTOs.CasoDefensaDTO;
import prog2.policia_backend.DTOs.DefendidoDTO;
import prog2.policia_backend.services.DefensaService;

@RestController
@RequestMapping("/api/defensa/defendidos")
@RequiredArgsConstructor
public class DefensaController {
    private final DefensaService defensaService;

    @GetMapping
    public List<DefendidoDTO> listarDefendidos() {
        return defensaService.listarDefendidos();
    }

    @GetMapping("/{codigo}")
    public DefendidoDTO consultarDefendido(@PathVariable String codigo) {
        return defensaService.consultarDefendido(codigo);
    }

    @GetMapping("/{codigo}/casos-judiciales")
    public List<CasoDefensaDTO> listarCasos(@PathVariable String codigo) {
        return defensaService.listarCasos(codigo);
    }

    @GetMapping("/{codigo}/casos-judiciales/{casoCodigo}")
    public CasoDefensaDTO consultarCaso(@PathVariable String codigo, @PathVariable String casoCodigo) {
        return defensaService.consultarCaso(codigo, casoCodigo);
    }
}
