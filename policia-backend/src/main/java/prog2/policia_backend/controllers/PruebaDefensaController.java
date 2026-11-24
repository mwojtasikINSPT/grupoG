package prog2.policia_backend.controllers;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prog2.policia_backend.DTOs.ConsultaPruebasDefensaDTO;
import prog2.policia_backend.DTOs.PresentacionPruebaDefensaDTO;
import prog2.policia_backend.DTOs.PruebaDefensaDTO;
import prog2.policia_backend.services.PruebaDefensaService;

@RestController
@RequestMapping("/api/pruebas-defensa")
@RequiredArgsConstructor
public class PruebaDefensaController {
    private final PruebaDefensaService pruebaDefensaService;

    @PostMapping
    public ResponseEntity<PruebaDefensaDTO> presentar(@Valid @RequestBody PresentacionPruebaDefensaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pruebaDefensaService.presentar(dto));
    }

    @GetMapping
    public List<PruebaDefensaDTO> consultar(@Valid @ModelAttribute ConsultaPruebasDefensaDTO dto) {
        return pruebaDefensaService.consultar(dto.getAsaltanteCodigo(), dto.getCasoCodigo());
    }
}
