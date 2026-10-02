package prog2.policia_backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prog2.policia_backend.DTOs.CasoJudicialDTO;
import prog2.policia_backend.services.CasoJudicialService;

import java.util.List;

@RestController
@RequestMapping("/api/casos-judiciales")
@RequiredArgsConstructor
public class CasoJudicialController {

    private final CasoJudicialService casoJudicialService;

    @GetMapping
    public ResponseEntity<List<CasoJudicialDTO>> listar() {
        return ResponseEntity.ok(casoJudicialService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CasoJudicialDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(casoJudicialService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CasoJudicialDTO> guardar(@Valid @RequestBody CasoJudicialDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(casoJudicialService.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CasoJudicialDTO> actualizar(
            @Valid @PathVariable Long id,
            @RequestBody CasoJudicialDTO dto) {

        return ResponseEntity.ok(casoJudicialService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        casoJudicialService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
