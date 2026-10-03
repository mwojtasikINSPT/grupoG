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
            @RequestParam(required = false) Boolean activo) {

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
            @Valid @RequestBody EntidadBancariaDTO dto) {

        return ResponseEntity.ok(
                entidadBancariaService.actualizar(codigo, dto)
        );
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String codigo) {

        entidadBancariaService.eliminar(codigo);
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
