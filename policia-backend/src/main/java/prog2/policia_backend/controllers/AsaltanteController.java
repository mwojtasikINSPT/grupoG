package prog2.policia_backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prog2.policia_backend.DTOs.AsaltanteDTO;
import prog2.policia_backend.services.AsaltanteService;

import java.util.List;

@RestController
@RequestMapping("/api/asaltantes")
@RequiredArgsConstructor
public class AsaltanteController {

    private final AsaltanteService asaltanteService;

    @GetMapping
    public ResponseEntity<List<AsaltanteDTO>> listar() {
        return ResponseEntity.ok(asaltanteService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsaltanteDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(asaltanteService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AsaltanteDTO> guardar(@RequestBody AsaltanteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(asaltanteService.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AsaltanteDTO> actualizar(
            @PathVariable Long id,
            @RequestBody AsaltanteDTO dto) {

        return ResponseEntity.ok(asaltanteService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        asaltanteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}