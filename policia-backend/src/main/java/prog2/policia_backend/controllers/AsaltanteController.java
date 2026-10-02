package prog2.policia_backend.controllers;

import jakarta.validation.Valid;
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

    /*
    @GetMapping("/{id}")
    public ResponseEntity<AsaltanteDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(asaltanteService.buscarPorId(id));
    }
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<AsaltanteDTO> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(asaltanteService.buscarPorCodigo(codigo));
    }

    @PostMapping
    public ResponseEntity<AsaltanteDTO> guardar(@Valid @RequestBody AsaltanteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(asaltanteService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<AsaltanteDTO> actualizar(
            @PathVariable String codigo,
            @Valid @RequestBody AsaltanteDTO dto) {

        return ResponseEntity.ok(asaltanteService.actualizar(codigo, dto));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        asaltanteService.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }
}
