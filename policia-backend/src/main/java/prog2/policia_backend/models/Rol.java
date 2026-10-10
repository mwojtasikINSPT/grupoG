package prog2.policia_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import prog2.policia_backend.validations.OnCreate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL)
    private List<Usuario> usuarios = new ArrayList<>();

    @ElementCollection(targetClass = Permiso.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "rol_permiso", joinColumns = @JoinColumn(name = "rol_id"))
    @Column(name = "permiso", nullable = false)
    @NotEmpty(groups = OnCreate.class)
    private Set<Permiso> permisos = new HashSet<>();
}
