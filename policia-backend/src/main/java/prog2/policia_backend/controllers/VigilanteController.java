package prog2.policia_backend.controllers;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import prog2.policia_backend.DTOs.ContratoVigilanciaDTO;
import prog2.policia_backend.services.ContratoVigilanciaService;
import prog2.policia_backend.DTOs.VigilanteDTO;
import prog2.policia_backend.services.VigilanteService;

@RestController
@RequestMapping("/api/vigilantes")
@RequiredArgsConstructor
public class VigilanteController {

    private final VigilanteService vigilanteService;
    private final ContratoVigilanciaService contratoVigilanciaService;

    @GetMapping
    public ResponseEntity<List<VigilanteDTO>> listar() {
        return ResponseEntity.ok(vigilanteService.listar());
    }

    @PreAuthorize( //para restricciones del id
            "hasAnyRole('INVESTIGADOR','ADMINISTRADOR') "
            + "or (hasRole('VIGILANTE') and "
            + "#id == authentication.principal.id)"
    )
    @GetMapping("/{id}")
    public ResponseEntity<VigilanteDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vigilanteService.buscarPorId(id));
    }

    @GetMapping("/{id}/contratos")
    @PreAuthorize(
            "hasAnyRole('INVESTIGADOR','ADMINISTRADOR') "
            + "or (hasRole('VIGILANTE') and "
            + "#id == authentication.principal.id)"
    )
    public ResponseEntity<List<ContratoVigilanciaDTO>> listarContratos(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                contratoVigilanciaService.listarPorVigilante(id)
        );
    }

    @PostMapping
    public ResponseEntity<VigilanteDTO> guardar(@Valid @RequestBody VigilanteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vigilanteService.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VigilanteDTO> actualizar(
            @PathVariable Long id,
            @RequestBody VigilanteDTO dto) {

        return ResponseEntity.ok(
                vigilanteService.actualizar(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        vigilanteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
