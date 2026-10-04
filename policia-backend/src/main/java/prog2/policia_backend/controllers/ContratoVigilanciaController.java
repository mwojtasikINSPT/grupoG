package prog2.policia_backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import prog2.policia_backend.DTOs.ContratoVigilanciaDTO;
import prog2.policia_backend.services.ContratoVigilanciaService;

import java.util.List;

@RestController
@RequestMapping("/api/contratos-vigilancia")
@RequiredArgsConstructor
public class ContratoVigilanciaController {

    private final ContratoVigilanciaService contratoVigilanciaService;

    @GetMapping
    public ResponseEntity<List<ContratoVigilanciaDTO>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String vigilante) {

        if (vigilante != null && !vigilante.isBlank()) {
            return ResponseEntity.ok(
                    contratoVigilanciaService.buscarPorNombreVigilante(
                            vigilante, activo)
            );
        }

        return ResponseEntity.ok(
                contratoVigilanciaService.listar(activo)
        );
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<ContratoVigilanciaDTO> buscarPorCodigo(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                contratoVigilanciaService.buscarPorCodigo(codigo)
        );
    }

    @PostMapping
    public ResponseEntity<ContratoVigilanciaDTO> guardar(
            @Valid @RequestBody ContratoVigilanciaDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contratoVigilanciaService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<ContratoVigilanciaDTO> actualizar(
            @PathVariable String codigo,
            @Valid @RequestBody ContratoVigilanciaDTO dto) {

        return ResponseEntity.ok(
                contratoVigilanciaService.actualizar(codigo, dto)
        );
    }

    @PatchMapping("/{codigo}/baja")
    public ResponseEntity<Void> eliminar(
            @PathVariable String codigo) {

        contratoVigilanciaService.eliminar(codigo);

        return ResponseEntity.noContent().build();
    }
}
