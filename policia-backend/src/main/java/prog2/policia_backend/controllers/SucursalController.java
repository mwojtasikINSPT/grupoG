package prog2.policia_backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import prog2.policia_backend.DTOs.SucursalDTO;
import prog2.policia_backend.services.SucursalService;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalService sucursalService;

    @GetMapping
    public ResponseEntity<List<SucursalDTO>> listar(
            @RequestParam(required = false) Boolean activo) {

        return ResponseEntity.ok(
                sucursalService.listar(activo)
        );
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<SucursalDTO> buscarPorCodigo(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                sucursalService.buscarPorCodigo(codigo)
        );
    }

    @PostMapping
    public ResponseEntity<SucursalDTO> guardar(
            @Valid @RequestBody SucursalDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sucursalService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<SucursalDTO> actualizar(
            @PathVariable String codigo,
            @Valid @RequestBody SucursalDTO dto) {

        return ResponseEntity.ok(
                sucursalService.actualizar(codigo, dto)
        );
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String codigo,
            @RequestBody SucursalDTO dto) {

        sucursalService.eliminar(codigo, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigo}/reactivar")
    public ResponseEntity<SucursalDTO> reactivar(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                sucursalService.reactivar(codigo)
        );
    }

}
