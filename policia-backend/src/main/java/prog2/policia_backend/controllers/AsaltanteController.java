package prog2.policia_backend.controllers;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import prog2.policia_backend.DTOs.AsaltanteDTO;
import prog2.policia_backend.DTOs.AsaltoDTO;
import prog2.policia_backend.services.AsaltanteService;
import prog2.policia_backend.DTOs.CasoJudicialDTO;
import prog2.policia_backend.services.AsaltoService;
import prog2.policia_backend.services.CasoJudicialService;

@RestController
@RequestMapping("/api/asaltantes")
@RequiredArgsConstructor
public class AsaltanteController {

    private final AsaltanteService asaltanteService;
    private final CasoJudicialService casoJudicialService;
    private final AsaltoService asaltoService;

    @GetMapping
    public ResponseEntity<List<AsaltanteDTO>> listar(
            @RequestParam(required = false) Boolean activo) {

        return ResponseEntity.ok(
                asaltanteService.listar(activo)
        );
    }

    /*
    @GetMapping("/{id}")
    public ResponseEntity<AsaltanteDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(asaltanteService.buscarPorId(id));
    }
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<AsaltanteDTO> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(asaltanteService.buscarPorCodigo(codigo));
    }

    @PostMapping
    public ResponseEntity<AsaltanteDTO> guardar(@Valid @RequestBody AsaltanteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(asaltanteService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<AsaltanteDTO> actualizar(
            @PathVariable String codigo,
            @Valid @RequestBody AsaltanteDTO dto) {

        return ResponseEntity.ok(asaltanteService.actualizar(codigo, dto));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        asaltanteService.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{codigo}/casos-judiciales")
    public ResponseEntity<List<CasoJudicialDTO>> listarCasosJudiciales(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                casoJudicialService.listarPorAsaltante(codigo)
        );
    }

    @GetMapping("/{codigo}/asaltos")
    public ResponseEntity<List<AsaltoDTO>> listarAsaltos(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                asaltoService.listarPorAsaltante(codigo)
        );
    }
}
