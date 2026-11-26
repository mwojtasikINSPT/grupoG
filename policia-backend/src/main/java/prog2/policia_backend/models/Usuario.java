package prog2.policia_backend.models;

import java.time.LocalDate;
import java.time.Period;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import prog2.policia_backend.exceptions.OperacionNoPermitidaException;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Usuario extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigo;

    private String nombre;
    private String password;

    @ManyToOne
    @Setter(AccessLevel.NONE)
    @JoinColumn(name = "rol_id", nullable = false, updatable = false)
    private Rol rol;

    private boolean activo = true;

    @Enumerated(EnumType.STRING)
    private MotivoBajaPersona motivoBaja;

    @Column(nullable = true)
    private LocalDate fechaNacimiento;

    public void asignarRolInicial(Rol rol) {
        if (rol == null) {
            throw new OperacionNoPermitidaException("El rol del usuario es obligatorio.");
        }
        if (this.rol != null) {
            throw new OperacionNoPermitidaException("No se puede cambiar el rol de un usuario existente.");
        }
        this.rol = rol;
    }

    public int getEdad() {
        if (fechaNacimiento == null) {
            return 0;
        }

        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }
}
