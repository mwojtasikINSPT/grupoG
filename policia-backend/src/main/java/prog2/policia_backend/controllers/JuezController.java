package prog2.policia_backend.controllers;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import prog2.policia_backend.DTOs.CasoJudicialDTO;
import prog2.policia_backend.DTOs.JuezDTO;
import prog2.policia_backend.services.CasoJudicialService;
import prog2.policia_backend.services.JuezService;

@RestController
@RequestMapping("/api/jueces")
@RequiredArgsConstructor
public class JuezController {

    private final JuezService juezService;
    private final CasoJudicialService casoJudicialService;

    @GetMapping
    public ResponseEntity<List<JuezDTO>> listar(
            @RequestParam(required = false) Boolean activo) {

        return ResponseEntity.ok(
                juezService.listar(activo)
        );
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<JuezDTO> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(juezService.buscarPorCodigo(codigo));
    }

    @GetMapping("/{codigo}/casos-judiciales")
    public ResponseEntity<List<CasoJudicialDTO>> listarCasosJudiciales(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                casoJudicialService.listarPorJuez(codigo)
        );
    }

    @PostMapping
    public ResponseEntity<JuezDTO> guardar(@Valid @RequestBody JuezDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(juezService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<JuezDTO> actualizar(
            @PathVariable String codigo,
            @Valid @RequestBody JuezDTO dto) {

        return ResponseEntity.ok(juezService.actualizar(codigo, dto));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String codigo,
            @RequestBody JuezDTO dto) {

        juezService.eliminar(codigo, dto);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigo}/reactivar")
    public ResponseEntity<JuezDTO> reactivar(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                juezService.reactivar(codigo)
        );
    }

}
