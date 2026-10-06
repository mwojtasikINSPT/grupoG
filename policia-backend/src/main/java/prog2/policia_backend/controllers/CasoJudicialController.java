package prog2.policia_backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import java.util.List;

import prog2.policia_backend.DTOs.CasoJudicialDTO;
import prog2.policia_backend.services.CasoJudicialService;

@RestController
@RequestMapping("/api/casos-judiciales")
@RequiredArgsConstructor
public class CasoJudicialController {

    private final CasoJudicialService casoJudicialService;

    @GetMapping
    public ResponseEntity<List<CasoJudicialDTO>> listar(
            @RequestParam(required = false) Boolean activo) {

        return ResponseEntity.ok(casoJudicialService.listar(activo));
    }

    @GetMapping("/asaltante/{codigo}")
    public ResponseEntity<List<CasoJudicialDTO>> listarPorAsaltante(@PathVariable String codigo) {

        return ResponseEntity.ok(casoJudicialService.listarPorAsaltante(codigo));
    }

    @GetMapping("/juez/{codigo}")
    public ResponseEntity<List<CasoJudicialDTO>> listarPorJuez(@PathVariable String codigo) {

        return ResponseEntity.ok(casoJudicialService.listarPorJuez(codigo));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<CasoJudicialDTO> buscarPorCodigo(@PathVariable String codigo) {

        return ResponseEntity.ok(casoJudicialService.buscarPorCodigo(codigo));
    }

    @PostMapping
    public ResponseEntity<CasoJudicialDTO> guardar(@Validated @RequestBody CasoJudicialDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(casoJudicialService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<CasoJudicialDTO> actualizar(@PathVariable String codigo, @RequestBody CasoJudicialDTO dto) {

        return ResponseEntity.ok(casoJudicialService.actualizar(codigo, dto));
    }

    @PatchMapping("/{codigo}/eliminar")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo, @RequestBody CasoJudicialDTO dto) {
        casoJudicialService.eliminar(codigo);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigo}/reactivar")
    public ResponseEntity<CasoJudicialDTO> reactivar(@PathVariable String codigo) {

        return ResponseEntity.ok(casoJudicialService.reactivar(codigo));
    }
}
