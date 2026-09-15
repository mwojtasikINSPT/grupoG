package grupog.sistemapolicia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "jueces")
public class Juez {

    @Id
    @Column(name = "clave_interna", length = 20)
    private String claveInterna;

    @Column(name = "anios_servicio", nullable = false)
    private int aniosServicio;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    public Juez() {
    }

    public Juez(String claveInterna, int aniosServicio, String nombre) {
        this.claveInterna = claveInterna;
        this.aniosServicio = aniosServicio;
        this.nombre = nombre;
    }

    public String getClaveInterna() {
        return claveInterna;
    }

    public void setClaveInterna(String claveInterna) {
        this.claveInterna = claveInterna;
    }

    public int getAniosServicio() {
        return aniosServicio;
    }

    public void setAniosServicio(int aniosServicio) {
        this.aniosServicio = aniosServicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
