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
    public ResponseEntity<List<CasoJudicialDTO>> listar(
            @RequestParam(required = false) Boolean activo) {

        return ResponseEntity.ok(
                casoJudicialService.listar(activo)
        );
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<CasoJudicialDTO> buscarPorCodigo(
            @PathVariable String codigo) {

        return ResponseEntity.ok(
                casoJudicialService.buscarPorCodigo(codigo)
        );
    }

    @PostMapping
    public ResponseEntity<CasoJudicialDTO> guardar(@Valid @RequestBody CasoJudicialDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(casoJudicialService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<CasoJudicialDTO> actualizar(
            @PathVariable String codigo,
            @Valid @RequestBody CasoJudicialDTO dto) {

        return ResponseEntity.ok(
                casoJudicialService.actualizar(codigo, dto)
        );
    }
}
