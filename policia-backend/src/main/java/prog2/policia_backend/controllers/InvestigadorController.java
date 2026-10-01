package prog2.policia_backend.controllers;

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
    public ResponseEntity<List<InvestigadorDTO>> listar() {
        return ResponseEntity.ok(investigadorService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestigadorDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(investigadorService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<InvestigadorDTO> guardar(
            @RequestBody InvestigadorDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(investigadorService.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvestigadorDTO> actualizar(
            @PathVariable Long id,
            @RequestBody InvestigadorDTO dto) {

        return ResponseEntity.ok(
                investigadorService.actualizar(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        investigadorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}