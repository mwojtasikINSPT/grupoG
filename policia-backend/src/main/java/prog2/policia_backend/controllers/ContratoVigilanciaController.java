package prog2.policia_backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prog2.policia_backend.DTOs.ContratoVigilanciaDTO;
import prog2.policia_backend.services.ContratoVigilanciaService;

import java.util.List;

@RestController
@RequestMapping("/api/contratos-vigilancia")
@RequiredArgsConstructor
public class ContratoVigilanciaController {

    private final ContratoVigilanciaService contratoVigilanciaService;

    @GetMapping
    public ResponseEntity<List<ContratoVigilanciaDTO>> listar() {
        return ResponseEntity.ok(contratoVigilanciaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratoVigilanciaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contratoVigilanciaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ContratoVigilanciaDTO> guardar(
            @RequestBody ContratoVigilanciaDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contratoVigilanciaService.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContratoVigilanciaDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ContratoVigilanciaDTO dto) {

        return ResponseEntity.ok(
                contratoVigilanciaService.actualizar(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        contratoVigilanciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}