package prog2.policia_backend.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import prog2.policia_backend.DTOs.UsuarioDTO;
import prog2.policia_backend.services.UsuarioService;
import prog2.policia_backend.validations.OnCreate;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String nombre) {

        if (nombre != null && !nombre.isBlank()) {
            return ResponseEntity.ok(usuarioService.buscarPorNombre(nombre, activo));
        }
        return ResponseEntity.ok(usuarioService.listar(activo));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<UsuarioDTO> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(usuarioService.buscarPorCodigo(codigo));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> guardar(@Validated(OnCreate.class) @RequestBody UsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<UsuarioDTO> actualizar(@PathVariable String codigo, @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizar(codigo, dto));
    }

    @PatchMapping("/{codigo}/baja")
    public ResponseEntity<UsuarioDTO> eliminar(@PathVariable String codigo, @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.eliminar(codigo, dto));
    }

    @PatchMapping("/{codigo}/reactivar")
    public ResponseEntity<UsuarioDTO> reactivar(@PathVariable String codigo) {
        return ResponseEntity.ok(usuarioService.reactivar(codigo));
    }
}
