package prog2.policia_backend.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import prog2.policia_backend.DTOs.AsaltoDTO;
import prog2.policia_backend.services.AsaltoService;
import prog2.policia_backend.validations.OnCreate;
import prog2.policia_backend.validations.OnUpdate;

@RestController
@RequestMapping("/api/asaltos")
@RequiredArgsConstructor
public class AsaltoController {

    private final AsaltoService asaltoService;

    @GetMapping
    public ResponseEntity<List<AsaltoDTO>> listar() {
        return ResponseEntity.ok(asaltoService.listar());
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<AsaltoDTO> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(asaltoService.buscarPorCodigo(codigo));
    }

    @GetMapping("/asaltante/{codigo}")
    public ResponseEntity<List<AsaltoDTO>> listarPorAsaltante(@PathVariable String codigo) {
        return ResponseEntity.ok(asaltoService.listarPorAsaltante(codigo));
    }

    @GetMapping("/sucursal/{codigo}")
    public ResponseEntity<List<AsaltoDTO>> listarPorSucursal(@PathVariable String codigo) {
        return ResponseEntity.ok(asaltoService.listarPorSucursal(codigo));
    }

    @PostMapping
    public ResponseEntity<AsaltoDTO> guardar(@Validated(OnCreate.class) @RequestBody AsaltoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asaltoService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<AsaltoDTO> actualizar(@PathVariable String codigo, @RequestBody AsaltoDTO dto) {
        return ResponseEntity.ok(asaltoService.actualizar(codigo, dto));
    }
}
