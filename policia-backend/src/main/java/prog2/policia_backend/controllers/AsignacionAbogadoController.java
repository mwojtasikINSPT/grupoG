package prog2.policia_backend.controllers;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import prog2.policia_backend.DTOs.AbogadoDisponibleDTO;
import prog2.policia_backend.DTOs.AsignacionAbogadoDTO;
import prog2.policia_backend.DTOs.ResultadoAsignacionAbogadoDTO;
import prog2.policia_backend.services.AsignacionAbogadoService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AsignacionAbogadoController {
    private final AsignacionAbogadoService service;

    @GetMapping("/usuarios/abogados-disponibles")
    public List<AbogadoDisponibleDTO> listarDisponibles() {
        return service.listarDisponibles();
    }

    @PutMapping("/asaltantes/{codigo}/abogado-defensor")
    public ResultadoAsignacionAbogadoDTO asignar(@PathVariable String codigo, @Valid @RequestBody AsignacionAbogadoDTO dto) {
        return service.asignar(codigo, dto.getAbogadoCodigo());
    }

    @DeleteMapping("/asaltantes/{codigo}/abogado-defensor")
    public ResultadoAsignacionAbogadoDTO desvincular(@PathVariable String codigo) {
        return service.desvincular(codigo);
    }
}
