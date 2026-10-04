package prog2.policia_backend.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

//Cada EB tiene varias sucursales
@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class EntidadBancaria extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String domicilioCentral;

    @OneToMany(mappedBy = "entidadBancaria")
    private List<Sucursal> sucursales;
    private boolean activo = true;
    private String codigo;

    @Enumerated(EnumType.STRING)
    private MotivoBajaEntidadBancaria motivoBaja;
}
