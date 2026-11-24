package prog2.policia_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/** Tabla separada: consultar una prueba nunca carga implicitamente su binario. */
@Entity
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArchivoPruebaDefensa extends EntidadAuditable {
    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prueba_id", nullable = false, updatable = false)
    private PruebaDefensa prueba;

    @Column(nullable = false, updatable = false)
    private String nombreOriginal;

    @Column(nullable = false, updatable = false)
    private String tipoContenido;

    @Column(nullable = false, updatable = false)
    private Long tamano;

    @Lob
    @Column(nullable = false, updatable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] contenido;

    public ArchivoPruebaDefensa(PruebaDefensa prueba, String nombreOriginal, String tipoContenido, byte[] contenido) {
        this.prueba = prueba;
        this.nombreOriginal = nombreOriginal;
        this.tipoContenido = tipoContenido;
        this.tamano = (long) contenido.length;
        this.contenido = contenido.clone();
    }
}
