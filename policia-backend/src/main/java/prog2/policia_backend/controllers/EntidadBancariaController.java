package prog2.policia_backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prog2.policia_backend.DTOs.EntidadBancariaDTO;
import prog2.policia_backend.services.EntidadBancariaService;

import java.util.List;

@RestController
@RequestMapping("/api/entidades-bancarias")
@RequiredArgsConstructor
public class EntidadBancariaController {

    private final EntidadBancariaService entidadBancariaService;

    @GetMapping
    public ResponseEntity<List<EntidadBancariaDTO>> listar() {
        return ResponseEntity.ok(entidadBancariaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntidadBancariaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(entidadBancariaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EntidadBancariaDTO> guardar(
            @RequestBody EntidadBancariaDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entidadBancariaService.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntidadBancariaDTO> actualizar(
            @PathVariable Long id,
            @RequestBody EntidadBancariaDTO dto) {

        return ResponseEntity.ok(
                entidadBancariaService.actualizar(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        entidadBancariaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}