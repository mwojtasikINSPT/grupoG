package prog2.policia_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import prog2.policia_backend.validations.OnCreate;
import prog2.policia_backend.exceptions.OperacionNoPermitidaException;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Rol extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigo;

    @Column(unique = true, nullable = false)
    @NotBlank(groups = OnCreate.class)
    private String nombre;

    @Column(nullable = false, unique = true)
    @NotBlank(groups = OnCreate.class)
    private String prefijo;

    private String descripcion;

    private Boolean activo = true;

    @OneToMany(mappedBy = "rol")
    @Setter(AccessLevel.NONE)
    private List<Usuario> usuarios = new ArrayList<>();

    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private Set<RolPermisoEntidad> permisosPorEntidad = new HashSet<>();

    public List<Usuario> getUsuarios() {
        return Collections.unmodifiableList(usuarios);
    }

    public Set<RolPermisoEntidad> getPermisosPorEntidad() {
        return Collections.unmodifiableSet(permisosPorEntidad);
    }

    public void actualizarPermisos(Collection<RolPermisoEntidad> nuevosPermisos) {
        if (nuevosPermisos == null || nuevosPermisos.isEmpty()) {
            throw new OperacionNoPermitidaException("El rol debe tener al menos un permiso");
        }
        Set<String> combinaciones = new HashSet<>();
        for (RolPermisoEntidad relacion : nuevosPermisos) {
            if (relacion == null || relacion.getPermiso() == null || relacion.getEntidad() == null) {
                throw new OperacionNoPermitidaException("El permiso y la entidad son obligatorios");
            }
            if (!combinaciones.add(clavePermiso(relacion))) {
                throw new OperacionNoPermitidaException("No se permiten permisos duplicados sobre una entidad");
            }
        }
        exigirLectura(combinaciones, Entidad.USUARIO, Entidad.ROL);
        exigirLectura(combinaciones, Entidad.ASALTO, Entidad.ASALTANTE);
        exigirLectura(combinaciones, Entidad.ASALTO, Entidad.SUCURSAL);
        exigirLectura(combinaciones, Entidad.CASO_JUDICIAL, Entidad.ASALTO);
        exigirLectura(combinaciones, Entidad.CASO_JUDICIAL, Entidad.JUEZ);
        exigirLectura(combinaciones, Entidad.SUCURSAL, Entidad.ENTIDAD_BANCARIA);

        Iterator<RolPermisoEntidad> iterator = permisosPorEntidad.iterator();
        while (iterator.hasNext()) {
            if (!combinaciones.contains(clavePermiso(iterator.next()))) {
                iterator.remove();
            }
        }
        for (RolPermisoEntidad nueva : nuevosPermisos) {
            boolean existe = false;
            for (RolPermisoEntidad actual : permisosPorEntidad) {
                if (actual.getPermiso() == nueva.getPermiso() && actual.getEntidad() == nueva.getEntidad()) {
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                permisosPorEntidad.add(new RolPermisoEntidad(this, nueva.getPermiso(), nueva.getEntidad()));
            }
        }
    }

    private String clavePermiso(RolPermisoEntidad relacion) {
        return relacion.getPermiso().name() + ":" + relacion.getEntidad().name();
    }

    private void exigirLectura(Set<String> combinaciones, Entidad entidadEscritura, Entidad entidadLectura) {
        String escritura = Permiso.ESCRITURA.name() + ":" + entidadEscritura.name();
        String lectura = Permiso.LECTURA.name() + ":" + entidadLectura.name();
        if (combinaciones.contains(escritura) && !combinaciones.contains(lectura)) {
            throw new OperacionNoPermitidaException("La escritura de " + entidadEscritura + " requiere lectura de " + entidadLectura + ".");
        }
    }

}
