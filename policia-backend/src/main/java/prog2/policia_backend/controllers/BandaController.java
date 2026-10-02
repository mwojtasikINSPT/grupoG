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
    public ResponseEntity<List<BandaDTO>> listar(
            @RequestParam(required = false) Boolean activo) {

        return ResponseEntity.ok(
                bandaService.listar(activo)
        );
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<BandaDTO> buscarPorCodigo(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                bandaService.buscarPorCodigo(codigo)
        );
    }

    @PostMapping
    public ResponseEntity<BandaDTO> guardar() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bandaService.guardar());
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String codigo) {

        bandaService.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigo}/reactivar")
    public ResponseEntity<BandaDTO> reactivar(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                bandaService.reactivar(codigo)
        );
    }
}
