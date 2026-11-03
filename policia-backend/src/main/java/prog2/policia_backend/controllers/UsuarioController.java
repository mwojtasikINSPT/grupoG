package prog2.policia_backend.controllers;

import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import prog2.policia_backend.DTOs.CambioPasswordDTO;
import prog2.policia_backend.DTOs.RestablecerPasswordDTO;
import prog2.policia_backend.DTOs.UsuarioDTO;
import prog2.policia_backend.services.UsuarioService;
import prog2.policia_backend.validations.OnCreate;
import prog2.policia_backend.validations.OnUpdate;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'LECTURA_USUARIO', 'LECTURA_DATOS_PROPIOS_USUARIO')")
    public ResponseEntity<List<UsuarioDTO>> listar(@RequestParam(required = false) Boolean activo, @RequestParam(required = false) String nombre, Authentication authentication) {

        boolean tieneLecturaCompleta = false;

        for (GrantedAuthority autoridad : authentication.getAuthorities()) {
            String permiso = autoridad.getAuthority();

            if (permiso.equals("ROLE_ADMINISTRADOR") || permiso.equals("LECTURA_USUARIO")) {
                tieneLecturaCompleta = true;
                break;
            }
        }

        if (!tieneLecturaCompleta) {
            return ResponseEntity.ok(usuarioService.listarPropio(authentication.getName(), activo, nombre));
        }

        if (nombre != null && !nombre.isBlank()) {
            return ResponseEntity.ok(usuarioService.buscarPorNombre(nombre, activo));
        }

        return ResponseEntity.ok(usuarioService.listar(activo));
    }

    @GetMapping("/{codigo}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR', 'LECTURA_USUARIO', 'LECTURA_DATOS_PROPIOS_USUARIO')")
    public ResponseEntity<UsuarioDTO> buscarPorCodigo(@PathVariable String codigo, Authentication authentication) {

        boolean tieneLecturaCompleta = false;

        for (GrantedAuthority autoridad : authentication.getAuthorities()) {
            String permiso = autoridad.getAuthority();

            if (permiso.equals("ROLE_ADMINISTRADOR") || permiso.equals("LECTURA_USUARIO")) {
                tieneLecturaCompleta = true;
                break;
            }
        }

        if (!tieneLecturaCompleta && !codigo.equalsIgnoreCase(authentication.getName())) {
            throw new AccessDeniedException("No tiene permiso para consultar este usuario");
        }

        return ResponseEntity.ok(usuarioService.buscarPorCodigo(codigo));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioDTO> guardar(@Validated(OnCreate.class) @RequestBody UsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioDTO> actualizar(@PathVariable String codigo, @Validated(OnUpdate.class) @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizar(codigo, dto));
    }

    @PatchMapping("/{codigo}/baja")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo, @RequestBody UsuarioDTO dto) {
        usuarioService.eliminar(codigo, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> cambiarPassword(@Valid @RequestBody CambioPasswordDTO dto) {
        usuarioService.cambiarPassword(dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigo}/password")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> restablecerPassword(@PathVariable String codigo, @Valid @RequestBody RestablecerPasswordDTO dto) {
        usuarioService.restablecerPassword(codigo, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigo}/reactivar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioDTO> reactivar(@PathVariable String codigo) {
        return ResponseEntity.ok(usuarioService.reactivar(codigo));
    }
}
