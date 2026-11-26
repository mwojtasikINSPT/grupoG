package prog2.policia_backend.models;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import prog2.policia_backend.exceptions.OperacionNoPermitidaException;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "rol_permiso_entidad", uniqueConstraints = @UniqueConstraint(columnNames = {"rol_id", "permiso", "entidad"}))
public class RolPermisoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Permiso permiso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Entidad entidad;

    public RolPermisoEntidad(Rol rol, Permiso permiso, Entidad entidad) {
        if (rol == null) {
            throw new OperacionNoPermitidaException("El rol es obligatorio.");
        }
        if (permiso == null || entidad == null) {
            throw new OperacionNoPermitidaException("El permiso y la entidad son obligatorios");
        }
        this.rol = rol;
        this.permiso = permiso;
        this.entidad = entidad;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof RolPermisoEntidad)) {
            return false;
        }

        RolPermisoEntidad otro = (RolPermisoEntidad) objeto;

        return Objects.equals(rol, otro.rol)
                && permiso == otro.permiso
                && entidad == otro.entidad;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rol, permiso, entidad);
    }
}
