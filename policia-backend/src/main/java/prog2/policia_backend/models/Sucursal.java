package prog2.policia_backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

//Cada S pertenece a una EB
@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Sucursal extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String domicilio;
    private Integer cantEmpleados;
    
    @ManyToOne
    private EntidadBancaria entidadBancaria;
    private boolean activo = true;
    private String codigo;

    @Enumerated(EnumType.STRING)
    private MotivoCierreSucursal motivoCierre;
}
