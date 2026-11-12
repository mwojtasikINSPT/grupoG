package prog2.policia_backend.controllers;


import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import prog2.policia_backend.DTOs.RolDTO;
import prog2.policia_backend.services.RolService;

import java.util.List;

import jakarta.validation.groups.Default;

import prog2.policia_backend.models.Entidad;
import prog2.policia_backend.validations.OnCreate;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    public ResponseEntity<List<RolDTO>> listar(@RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(rolService.listar(activo));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<RolDTO> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(rolService.buscarPorCodigo(codigo));
    }

    @PostMapping
    public ResponseEntity<RolDTO> guardar(@Validated({OnCreate.class, Default.class}) @RequestBody RolDTO rolDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.guardar(rolDTO));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<RolDTO> actualizar(@PathVariable String codigo, @RequestBody RolDTO rolDTO) {
        return ResponseEntity.ok(rolService.actualizar(codigo, rolDTO));
    }

    @PatchMapping("/{codigo}/baja")
    public ResponseEntity<RolDTO> eliminar(@PathVariable String codigo) {
        rolService.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigo}/reactivar")
    public ResponseEntity<RolDTO> reactivar(@PathVariable String codigo) {

        return ResponseEntity.ok(rolService.reactivar(codigo));
    }

    @GetMapping("/entidades")
    public ResponseEntity<List<Entidad>> listarEntidades() {
        return ResponseEntity.ok(List.of(Entidad.values()));
    }
}
