package prog2.policia_backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prog2.policia_backend.DTOs.VigilanteDTO;
import prog2.policia_backend.services.VigilanteService;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/vigilantes")
@RequiredArgsConstructor
public class VigilanteController {

    private final VigilanteService vigilanteService;

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

    @PostMapping
    public ResponseEntity<VigilanteDTO> guardar(@RequestBody VigilanteDTO dto) {
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
