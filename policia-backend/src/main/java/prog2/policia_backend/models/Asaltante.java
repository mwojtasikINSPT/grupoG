package prog2.policia_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import prog2.policia_backend.exceptions.OperacionNoPermitidaException;
import prog2.policia_backend.exceptions.RolAbogadoInvalidoException;
import prog2.policia_backend.exceptions.AbogadoInactivoException;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Asaltante extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;

    @ManyToOne
    private Banda banda;

    @ManyToOne(fetch = FetchType.LAZY)
    @Setter(AccessLevel.NONE)
    @JoinColumn(name = "abogado_defensor_id")
    private Usuario abogadoDefensor;
    @Column(unique = true)
    private String codigo;
    private boolean activo = true;

    @Enumerated(EnumType.STRING)
    private MotivoBajaPersona motivoBaja;

    public void asignarAbogadoDefensor(Usuario abogado) {
        if (abogado == null) {
            throw new OperacionNoPermitidaException("El abogado defensor es obligatorio para asignarlo.");
        }
        if (abogado.getRol() == null || !abogado.getRol().isHabilitadoParaDefensa()
                || !Boolean.TRUE.equals(abogado.getRol().getActivo())) {
            throw new RolAbogadoInvalidoException();
        }
        if (!abogado.isActivo()) {
            throw new AbogadoInactivoException();
        }
        this.abogadoDefensor = abogado;
    }

    public void desvincularAbogadoDefensor() {
        this.abogadoDefensor = null;
    }
}
