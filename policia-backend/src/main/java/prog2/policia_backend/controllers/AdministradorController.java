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
    public ResponseEntity<List<AdministradorDTO>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String nombre) {

        if (nombre != null && !nombre.isBlank()) {
            return ResponseEntity.ok(
                    administradorService.buscarPorNombre(nombre, activo)
            );
        }

        return ResponseEntity.ok(
                administradorService.listar(activo)
        );
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<AdministradorDTO> buscarPorCodigo(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                administradorService.buscarPorCodigo(codigo)
        );
    }

    @PostMapping
    public ResponseEntity<AdministradorDTO> guardar(
            @Valid @RequestBody AdministradorDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(administradorService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<AdministradorDTO> actualizar(
            @PathVariable String codigo,
            @RequestBody AdministradorDTO dto) {

        return ResponseEntity.ok(
                administradorService.actualizar(codigo, dto)
        );
    }

    @PatchMapping("/{codigo}/baja")
    public ResponseEntity<Void> eliminar(
            @PathVariable String codigo,
            @RequestBody AdministradorDTO dto) {

        administradorService.eliminar(codigo, dto);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigo}/reactivar")
    public ResponseEntity<AdministradorDTO> reactivar(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                administradorService.reactivar(codigo)
        );
    }
}
