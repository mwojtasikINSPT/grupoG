package prog2.policia_backend.models;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
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
