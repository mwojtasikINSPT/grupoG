package prog2.policia_backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import prog2.policia_backend.DTOs.SucursalDTO;
import prog2.policia_backend.services.SucursalService;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalService sucursalService;

    @GetMapping
    public ResponseEntity<List<SucursalDTO>> listar() {
        return ResponseEntity.ok(sucursalService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SucursalDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sucursalService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<SucursalDTO> guardar(@Valid @RequestBody SucursalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sucursalService.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SucursalDTO> actualizar(
            @Valid @PathVariable Long id,
            @RequestBody SucursalDTO dto) {

        return ResponseEntity.ok(
                sucursalService.actualizar(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        sucursalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}