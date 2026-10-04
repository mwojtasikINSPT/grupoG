package prog2.policia_backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import prog2.policia_backend.DTOs.InvestigadorDTO;
import prog2.policia_backend.services.InvestigadorService;

import java.util.List;

@RestController
@RequestMapping("/api/investigadores")
@RequiredArgsConstructor
public class InvestigadorController {

    private final InvestigadorService investigadorService;

    @GetMapping
    public ResponseEntity<List<InvestigadorDTO>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String nombre) {

        if (nombre != null && !nombre.isBlank()) {
            return ResponseEntity.ok(
                    investigadorService.buscarPorNombre(nombre, activo)
            );
        }

        return ResponseEntity.ok(
                investigadorService.listar(activo)
        );
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<InvestigadorDTO> buscarPorCodigo(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                investigadorService.buscarPorCodigo(codigo)
        );
    }

    @PostMapping
    public ResponseEntity<InvestigadorDTO> guardar(
            @Valid @RequestBody InvestigadorDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(investigadorService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<InvestigadorDTO> actualizar(
            @PathVariable String codigo,
            @RequestBody InvestigadorDTO dto) {

        return ResponseEntity.ok(
                investigadorService.actualizar(codigo, dto)
        );
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String codigo,
            @RequestBody InvestigadorDTO dto) {

        investigadorService.eliminar(codigo, dto);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigo}/reactivar")
    public ResponseEntity<InvestigadorDTO> reactivar(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                investigadorService.reactivar(codigo)
        );
    }
}
