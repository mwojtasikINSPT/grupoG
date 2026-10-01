package prog2.policia_backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prog2.policia_backend.DTOs.AsaltoDTO;
import prog2.policia_backend.services.AsaltoService;

import java.util.List;

@RestController
@RequestMapping("/api/asaltos")
@RequiredArgsConstructor
public class AsaltoController {

    private final AsaltoService asaltoService;

    @GetMapping
    public ResponseEntity<List<AsaltoDTO>> listar() {
        return ResponseEntity.ok(asaltoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsaltoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(asaltoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AsaltoDTO> guardar(@RequestBody AsaltoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(asaltoService.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AsaltoDTO> actualizar(
            @PathVariable Long id,
            @RequestBody AsaltoDTO dto) {

        return ResponseEntity.ok(asaltoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        asaltoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}