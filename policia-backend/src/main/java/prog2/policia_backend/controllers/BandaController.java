package prog2.policia_backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import prog2.policia_backend.DTOs.BandaDTO;
import prog2.policia_backend.services.BandaService;

import java.util.List;

@RestController
@RequestMapping("/api/bandas")
@RequiredArgsConstructor
public class BandaController {

    private final BandaService bandaService;

    @GetMapping
    public ResponseEntity<List<BandaDTO>> listar() {
        return ResponseEntity.ok(bandaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BandaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(bandaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<BandaDTO> guardar(@RequestBody BandaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bandaService.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BandaDTO> actualizar(
            @PathVariable Long id,
            @RequestBody BandaDTO dto) {

        return ResponseEntity.ok(bandaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        bandaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}