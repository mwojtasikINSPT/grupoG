package prog2.policia_backend.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import prog2.policia_backend.DTOs.EntidadBancariaDTO;
import prog2.policia_backend.DTOs.SucursalDTO;
import prog2.policia_backend.services.EntidadBancariaService;
import prog2.policia_backend.services.SucursalService;

@RestController
@RequestMapping("/api/entidades-bancarias")
@RequiredArgsConstructor
public class EntidadBancariaController {

    private final EntidadBancariaService entidadBancariaService;
    private final SucursalService sucursalService;

    @GetMapping
    public ResponseEntity<List<EntidadBancariaDTO>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String domicilio) {

        if (nombre != null && !nombre.isBlank()) {
            return ResponseEntity.ok(
                    entidadBancariaService.buscarPorNombre(nombre, activo)
            );
        }

        if (domicilio != null && !domicilio.isBlank()) {
            return ResponseEntity.ok(
                    entidadBancariaService.buscarPorDomicilio(domicilio, activo)
            );
        }

        return ResponseEntity.ok(
                entidadBancariaService.listar(activo)
        );
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<EntidadBancariaDTO> buscarPorCodigo(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                entidadBancariaService.buscarPorCodigo(codigo)
        );
    }

    @GetMapping("/{codigo}/sucursales")
    public ResponseEntity<List<SucursalDTO>> listarSucursales(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                sucursalService.listarPorEntidadBancaria(codigo)
        );
    }

    @PostMapping
    public ResponseEntity<EntidadBancariaDTO> guardar(
            @Valid @RequestBody EntidadBancariaDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entidadBancariaService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<EntidadBancariaDTO> actualizar(
            @PathVariable String codigo,
            @RequestBody EntidadBancariaDTO dto) {

        return ResponseEntity.ok(
                entidadBancariaService.actualizar(codigo, dto)
        );
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String codigo,
            @RequestBody EntidadBancariaDTO dto) {

        entidadBancariaService.eliminar(codigo, dto);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigo}/reactivar")
    public ResponseEntity<EntidadBancariaDTO> reactivar(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                entidadBancariaService.reactivar(codigo)
        );
    }
}
