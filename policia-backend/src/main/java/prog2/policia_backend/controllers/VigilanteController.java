package prog2.policia_backend.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import prog2.policia_backend.DTOs.ContratoVigilanciaDTO;
import prog2.policia_backend.services.ContratoVigilanciaService;
import prog2.policia_backend.DTOs.VigilanteDTO;
import prog2.policia_backend.services.VigilanteService;
import prog2.policia_backend.validations.OnCreate;
import prog2.policia_backend.validations.OnUpdate;

@RestController
@RequestMapping("/api/vigilantes")
@RequiredArgsConstructor
public class VigilanteController {

    private final VigilanteService vigilanteService;
    private final ContratoVigilanciaService contratoVigilanciaService;

    @GetMapping
    public ResponseEntity<List<VigilanteDTO>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String nombre) {

        if (nombre != null && !nombre.isBlank()) {
            return ResponseEntity.ok(vigilanteService.buscarPorNombre(nombre, activo));
        }

        return ResponseEntity.ok(vigilanteService.listar(activo));
    }

    @PreAuthorize(
            "hasAuthority('LECTURA') "
            + "or (hasAuthority('LECTURA_DATOS_PROPIOS') "
            + "and #codigo == authentication.principal.codigo)"
    )
    @GetMapping("/{codigo}")
    public ResponseEntity<VigilanteDTO> buscarPorCodigo(@PathVariable String codigo) {

        return ResponseEntity.ok(vigilanteService.buscarPorCodigo(codigo));
    }

    @GetMapping("/{codigo}/contratos")
    @PreAuthorize(
            "hasAuthority('LECTURA') "
            + "or (hasAuthority('LECTURA_DATOS_PROPIOS') "
            + "and #codigo == authentication.principal.codigo)"
    )
    public ResponseEntity<List<ContratoVigilanciaDTO>> listarContratos(@PathVariable String codigo) {

        return ResponseEntity.ok(contratoVigilanciaService.listarPorVigilante(codigo));
    }

    @PostMapping
    public ResponseEntity<VigilanteDTO> guardar(@Validated(OnCreate.class) @RequestBody VigilanteDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(vigilanteService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<VigilanteDTO> actualizar(@PathVariable String codigo, @Validated(OnUpdate.class) @RequestBody VigilanteDTO dto) {

        return ResponseEntity.ok(vigilanteService.actualizar(codigo, dto));
    }

    @PatchMapping("/{codigo}/baja")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo, @RequestBody VigilanteDTO dto) {

        vigilanteService.eliminar(codigo, dto);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{codigo}/reactivar")
    public ResponseEntity<VigilanteDTO> reactivar(@PathVariable String codigo) {

        return ResponseEntity.ok(vigilanteService.reactivar(codigo));
    }
}
