
package grupog.sistemapolicia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vigilantes")
public class Vigilante {

    @Id
    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(name = "edad", nullable = false)
    private int edad;

    public Vigilante() {
    }

    public Vigilante(String codigo, int edad) {
        this.codigo = codigo;
        this.edad = edad;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    @Override
    public String toString() {
        return "Vigilante ["
                + "Código: " + codigo
                + ", Edad: " + edad
                + "]";
    }
}