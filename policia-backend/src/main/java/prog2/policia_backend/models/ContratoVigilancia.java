package prog2.policia_backend.models;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

//relación entre Vigilante y Sucursal: Vigilante 1 - N ContratoVigilancia N ─ 1 Sucursal
@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ContratoVigilancia extends EntidadAuditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;
    private Boolean conArma;
    private String codigo;

    @ManyToOne
    private Vigilante vigilante;

    @ManyToOne
    private Sucursal sucursal;

    private Boolean activo = true;

    @Enumerated(EnumType.STRING)
    private MotivoBajaContrato motivoBaja;
}
