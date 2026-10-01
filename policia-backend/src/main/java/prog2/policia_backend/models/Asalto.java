package prog2.policia_backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

//relaciona Asaltante con Sucursal: Asaltante 1 ─ N Asalto N ─ 1 Sucursal
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asalto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate fecha;
    private String codigo;
    @ManyToOne
    private Asaltante asaltante;
    @ManyToOne
    private Sucursal sucursal;
    private boolean activo = true;
}
