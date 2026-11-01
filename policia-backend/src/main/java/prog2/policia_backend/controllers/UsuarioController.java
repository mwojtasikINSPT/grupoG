package prog2.policia_backend.controllers;

import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import prog2.policia_backend.DTOs.CambioPasswordDTO;
import prog2.policia_backend.DTOs.UsuarioDTO;
import prog2.policia_backend.services.UsuarioService;
import prog2.policia_backend.utils.SeguridadUtils;
import prog2.policia_backend.validations.OnCreate;
import prog2.policia_backend.validations.OnUpdate;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('LECTURA', 'LECTURA_DATOS_PROPIOS')")
    public ResponseEntity<List<UsuarioDTO>> listar(@RequestParam(required = false) Boolean activo, @RequestParam(required = false) String nombre, Authentication authentication) {

        boolean tieneLecturaCompleta = SeguridadUtils.tieneLecturaCompleta(authentication);

        if (!tieneLecturaCompleta) {
            return ResponseEntity.ok(usuarioService.listarPropio(authentication.getName(), activo, nombre));
        }

        if (nombre != null && !nombre.isBlank()) {
            return ResponseEntity.ok(usuarioService.buscarPorNombre(nombre, activo));
        }

        return ResponseEntity.ok(usuarioService.listar(activo));
    }

    @GetMapping("/{codigo}")
    @PreAuthorize("hasAuthority('LECTURA') or (hasAuthority('LECTURA_DATOS_PROPIOS') and #codigo == authentication.principal.codigo)")
    public ResponseEntity<UsuarioDTO> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(usuarioService.buscarPorCodigo(codigo));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> guardar(@Validated(OnCreate.class) @RequestBody UsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<UsuarioDTO> actualizar(@PathVariable String codigo, @Validated(OnUpdate.class) @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizar(codigo, dto));
    }

    @PatchMapping("/{codigo}/baja")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo, @RequestBody UsuarioDTO dto) {
        usuarioService.eliminar(codigo, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> cambiarPassword(@Valid @RequestBody CambioPasswordDTO dto) {
        usuarioService.cambiarPassword(dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigo}/reactivar")
    public ResponseEntity<UsuarioDTO> reactivar(@PathVariable String codigo) {
        return ResponseEntity.ok(usuarioService.reactivar(codigo));
    }
}
