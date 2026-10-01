package prog2.policia_backend.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

//Cada EB tiene varias sucursales
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntidadBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String domicilioCentral;

    @OneToMany(mappedBy = "entidadBancaria")
    private List<Sucursal> sucursales;
    private boolean activo = true;
    private String codigo;
}
