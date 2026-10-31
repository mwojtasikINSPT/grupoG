package prog2.policia_backend.controllers;

import java.util.List;
import lombok.Builder.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import prog2.policia_backend.DTOs.CasoJudicialDTO;
import prog2.policia_backend.DTOs.SentenciaAsaltanteDTO;
import prog2.policia_backend.services.CasoJudicialService;
import prog2.policia_backend.validations.OnCreate;

// Expone los endpoints REST para gestionar casos judiciales y sus sentencias.
@RestController
@RequestMapping("/api/casos-judiciales")
@RequiredArgsConstructor
public class CasoJudicialController {

    private final CasoJudicialService casoJudicialService;

    @GetMapping
    public ResponseEntity<List<CasoJudicialDTO>> listar(@RequestParam(required = false) Boolean activo) {
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
    public ResponseEntity<CasoJudicialDTO> guardar(@Validated({OnCreate.class, Default.class}) @RequestBody CasoJudicialDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(casoJudicialService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<CasoJudicialDTO> actualizar(@PathVariable String codigo, @Validated(Default.class) @RequestBody CasoJudicialDTO dto) {
        return ResponseEntity.ok(casoJudicialService.actualizar(codigo, dto));
    }

    @PatchMapping("/{codigo}/eliminar")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        casoJudicialService.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigo}/reactivar")
    public ResponseEntity<CasoJudicialDTO> reactivar(@PathVariable String codigo) {
        return ResponseEntity.ok(casoJudicialService.reactivar(codigo));
    }

    @PostMapping("/{codigo}/sentencias")
    public ResponseEntity<Void> registrarSentencia(@PathVariable String codigo, @Validated(OnCreate.class) @RequestBody SentenciaAsaltanteDTO dto) {
        casoJudicialService.registrarSentencia(codigo, dto.getAsaltanteCodigo(), dto.getCondenado(), dto.getTiempoCarcel());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{codigo}/sentencias/{asaltanteCodigo}")
    public ResponseEntity<Void> modificarSentencia(@PathVariable String codigo, @PathVariable String asaltanteCodigo, @Validated(OnCreate.class) @RequestBody SentenciaAsaltanteDTO dto) {
        casoJudicialService.modificarSentencia(codigo, asaltanteCodigo, dto.getCondenado(), dto.getTiempoCarcel());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{codigo}/sentencias")
    public ResponseEntity<List<SentenciaAsaltanteDTO>> listarSentenciasPorCaso(@PathVariable String codigo) {
        return ResponseEntity.ok(casoJudicialService.listarSentenciasPorCaso(codigo));
    }

    @GetMapping("/sentencias/asaltante/{codigo}")
    public ResponseEntity<List<SentenciaAsaltanteDTO>> listarSentenciasPorAsaltante(@PathVariable String codigo) {
        return ResponseEntity.ok(casoJudicialService.listarSentenciasPorAsaltante(codigo));
    }
}
