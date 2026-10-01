package prog2.policia_backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prog2.policia_backend.DTOs.JuezDTO;
import prog2.policia_backend.services.JuezService;

import java.util.List;

@RestController
@RequestMapping("/api/jueces")
@RequiredArgsConstructor
public class JuezController {

    private final JuezService juezService;

    @GetMapping
    public ResponseEntity<List<JuezDTO>> listar() {
        return ResponseEntity.ok(juezService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JuezDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(juezService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<JuezDTO> guardar(@RequestBody JuezDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(juezService.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JuezDTO> actualizar(
            @PathVariable Long id,
            @RequestBody JuezDTO dto) {

        return ResponseEntity.ok(juezService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        juezService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}