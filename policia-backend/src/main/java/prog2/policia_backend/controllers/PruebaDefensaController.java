package prog2.policia_backend.controllers;

import java.util.List;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import prog2.policia_backend.DTOs.ArchivoPruebaDTO;
import prog2.policia_backend.DTOs.ConsultaPruebasDefensaDTO;
import prog2.policia_backend.DTOs.PresentacionPruebaDefensaDTO;
import prog2.policia_backend.DTOs.PruebaDefensaDTO;
import prog2.policia_backend.services.PruebaDefensaService;

@RestController
@RequestMapping("/api/pruebas-defensa")
@RequiredArgsConstructor
public class PruebaDefensaController {
    private final PruebaDefensaService pruebaDefensaService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PruebaDefensaDTO> presentar(@Valid @RequestBody PresentacionPruebaDefensaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pruebaDefensaService.presentar(dto));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PruebaDefensaDTO> presentarMultipart(@Valid @ModelAttribute PresentacionPruebaDefensaDTO dto,
            MultipartHttpServletRequest solicitud) {
        List<MultipartFile> archivos = new ArrayList<>();
        for (List<MultipartFile> parte : solicitud.getMultiFileMap().values()) {
            archivos.addAll(parte);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(pruebaDefensaService.presentar(dto, archivos));
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id) {
        ArchivoPruebaDTO archivo = pruebaDefensaService.descargar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.getTipoContenido()))
                .contentLength(archivo.getTamano())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(archivo.getNombreOriginal(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noStore())
                .body(archivo.getContenido());
    }

    @GetMapping
    public List<PruebaDefensaDTO> consultar(@Valid @ModelAttribute ConsultaPruebasDefensaDTO dto) {
        return pruebaDefensaService.consultar(dto.getAsaltanteCodigo(), dto.getCasoCodigo());
    }
}
