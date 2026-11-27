package prog2.policia_backend.controllers;

import java.util.List;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import prog2.policia_backend.DTOs.UsuarioDTO;
import prog2.policia_backend.models.MotivoBajaContrato;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import prog2.policia_backend.DTOs.ContratoVigilanciaDTO;
import prog2.policia_backend.services.ContratoVigilanciaService;
import prog2.policia_backend.validations.OnCreate;
import prog2.policia_backend.validations.OnUpdate;

@RestController
@RequestMapping("/api/contratos-vigilancia")
@RequiredArgsConstructor
public class ContratoVigilanciaController {

    private final ContratoVigilanciaService contratoVigilanciaService;

    @GetMapping("/motivos-baja")
    public ResponseEntity<List<MotivoBajaContrato>> listarMotivosBaja() {
        return ResponseEntity.ok(List.of(MotivoBajaContrato.values()));
    }

    @GetMapping("/vigilantes-disponibles")
    @PreAuthorize("hasAuthority('ESCRITURA_CONTRATO_VIGILANCIA') and hasAuthority('LECTURA_USUARIO')")
    public ResponseEntity<List<UsuarioDTO>> listarVigilantesDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String contratoCodigo) {
        return ResponseEntity.ok(contratoVigilanciaService.listarVigilantesDisponibles(fecha, contratoCodigo));
    }

    @GetMapping
    public ResponseEntity<List<ContratoVigilanciaDTO>> listar(Authentication authentication) {
        return ResponseEntity.ok(contratoVigilanciaService.listar(authentication));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<ContratoVigilanciaDTO> buscarPorCodigo(@PathVariable String codigo, Authentication authentication) {

        ContratoVigilanciaDTO contrato = contratoVigilanciaService.buscarPorCodigo(codigo);
        List<ContratoVigilanciaDTO> contratos = contratoVigilanciaService.filtrarPorPermisos(List.of(contrato), authentication);

        if (contratos.isEmpty()) {
            throw new AccessDeniedException("No tiene permisos para consultar este contrato");
        }

        return ResponseEntity.ok(contratos.get(0));
    }

    @GetMapping("/vigilante/{codigo}")
    public ResponseEntity<List<ContratoVigilanciaDTO>> listarPorVigilante(@PathVariable String codigo, Authentication authentication) {

        List<ContratoVigilanciaDTO> contratos = contratoVigilanciaService.listarPorVigilante(codigo);
        return ResponseEntity.ok(contratoVigilanciaService.filtrarPorPermisos(contratos, authentication));
    }

    @GetMapping("/sucursal/{codigo}")
    public ResponseEntity<List<ContratoVigilanciaDTO>> listarPorSucursal(@PathVariable String codigo, Authentication authentication) {

        List<ContratoVigilanciaDTO> contratos = contratoVigilanciaService.listarPorSucursal(codigo);
        return ResponseEntity.ok(contratoVigilanciaService.filtrarPorPermisos(contratos, authentication));
    }

    @PostMapping
    public ResponseEntity<ContratoVigilanciaDTO> guardar(@Validated(OnCreate.class) @RequestBody ContratoVigilanciaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contratoVigilanciaService.guardar(dto));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<ContratoVigilanciaDTO> actualizar(@PathVariable String codigo, @Validated(OnUpdate.class) @RequestBody ContratoVigilanciaDTO dto) {
        return ResponseEntity.ok(contratoVigilanciaService.actualizar(codigo, dto));
    }

    @PatchMapping("/{codigo}/baja")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo, @RequestBody ContratoVigilanciaDTO dto) {
        contratoVigilanciaService.eliminar(codigo, dto);
        return ResponseEntity.noContent().build();
    }
}
