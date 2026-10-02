package prog2.policia_backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prog2.policia_backend.DTOs.AdministradorDTO;
import prog2.policia_backend.services.AdministradorService;

import java.util.List;

@RestController
@RequestMapping("/api/administradores")
@RequiredArgsConstructor
public class AdministradorController {

    private final AdministradorService administradorService;

    @GetMapping
    public ResponseEntity<List<AdministradorDTO>> listar() {
        return ResponseEntity.ok(administradorService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministradorDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(administradorService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AdministradorDTO> guardar(
            @Valid @RequestBody AdministradorDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(administradorService.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdministradorDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AdministradorDTO dto) {

        return ResponseEntity.ok(
                administradorService.actualizar(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        administradorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}