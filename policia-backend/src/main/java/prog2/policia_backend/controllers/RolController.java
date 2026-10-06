package prog2.policia_backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prog2.policia_backend.DTOs.RolDTO;
import prog2.policia_backend.services.RolService;

import java.util.List;

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
    public ResponseEntity<RolDTO> guardar(@Valid @RequestBody RolDTO rolDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.guardar(rolDTO));
    }

    @PatchMapping("/{codigo}")
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
}
