
package grupog.sistemapolicia.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity   // clase q representa datos de mysql
@Table(name = "entidades_bancarias") // nombre de la tabla
public class EntidadBancaria {

    @Id  //nombre de la clave primaria o PRIMARY KEY
    @Column(name = "codigo", length = 20) //configura esa columna
    private String codigo;

    @Column(
        name = "domicilio_central",
        length = 150,
        nullable = false //dice q domic es obligatorio
    )
    private String domicilioCentral;

    public EntidadBancaria() {
    }

    public EntidadBancaria(
            String codigo,
            String domicilioCentral) {

        this.codigo = codigo;
        this.domicilioCentral = domicilioCentral;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDomicilioCentral() {
        return domicilioCentral;
    }

    public void setDomicilioCentral(String domicilioCentral) {
        this.domicilioCentral = domicilioCentral;
    }

    @Override
    public String toString() {
        return "Entidad Bancaria ["
                + "Código: " + codigo
                + ", Domicilio Central: " + domicilioCentral
                + "]";
    }
}