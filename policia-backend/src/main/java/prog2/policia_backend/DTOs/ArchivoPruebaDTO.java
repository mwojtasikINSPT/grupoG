package prog2.policia_backend.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Transporte interno para validar, persistir y descargar; no serializa el binario. */
@Getter
@AllArgsConstructor
public class ArchivoPruebaDTO {
    private String nombreOriginal;
    private String tipoContenido;
    private Long tamano;
    @JsonIgnore
    private byte[] contenido;
}
