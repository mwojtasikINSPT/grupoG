package prog2.policia_backend.controllers;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prog2.policia_backend.DTOs.AsaltoDTO;

import prog2.policia_backend.DTOs.ContratoVigilanciaDTO;
import prog2.policia_backend.DTOs.SucursalDTO;
import prog2.policia_backend.services.AsaltoService;
import prog2.policia_backend.services.SucursalService;
import prog2.policia_backend.services.ContratoVigilanciaService;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalService sucursalService;
    private final ContratoVigilanciaService contratoVigilanciaService;
    private final AsaltoService asaltoService;

    @GetMapping
    public ResponseEntity<List<SucursalDTO>> listar(@RequestParam(required = false) Boolean activo, @RequestParam(required = false) String domicilio) {

        if (domicilio != null && !domicilio.isBlank()) {
            return ResponseEntity.ok(sucursalService.buscarPorDomicilio(domicilio, activo));
        }

        return ResponseEntity.ok(sucursalService.listar(activo));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<SucursalDTO> buscarPorCodigo(@PathVariable String codigo) {

        return ResponseEntity.ok(sucursalService.buscarPorCodigo(codigo));
    }

    @GetMapping("/{codigo}/contratos")
    public ResponseEntity<List<ContratoVigilanciaDTO>> listarContratos(@PathVariable String codigo) {

        return ResponseEntity.ok(contratoVigilanciaService.listarPorSucursal(codigo));
    }

    @GetMapping("/{codigo}/asaltos")
    public ResponseEntity<List<AsaltoDTO>> listarAsaltos(@PathVariable String codigo) {

        return ResponseEntity.ok(asaltoService.listarPorSucursal(codigo));
    }

    @PostMapping
    public ResponseEntity<SucursalDTO> guardar(@Valid @RequestBody SucursalDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sucursalService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<SucursalDTO> actualizar(
            @PathVariable String codigo,
            @RequestBody SucursalDTO dto) {

        return ResponseEntity.ok(sucursalService.actualizar(codigo, dto));
    }

    @PatchMapping("/{codigo}/baja")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo, @RequestBody SucursalDTO dto) {

        sucursalService.eliminar(codigo, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigo}/reactivar")
    public ResponseEntity<SucursalDTO> reactivar(@PathVariable String codigo) {
        return ResponseEntity.ok(sucursalService.reactivar(codigo));
    }

}
