
package grupog.sistemapolicia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sucursales")
public class Sucursal {

    @Id
    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(
        name = "domicilio",
        length = 150,
        nullable = false
    )
    private String domicilio;

    @Column(
        name = "numero_empleados",
        nullable = false
    )
    private int numeroEmpleados;

    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY
    )
    @JoinColumn(
        name = "entidad_codigo",
        nullable = false
    )
    private EntidadBancaria entidad;

    public Sucursal() {
    }

    public Sucursal(
            String codigo,
            String domicilio,
            int numeroEmpleados,
            EntidadBancaria entidad) {

        this.codigo = codigo;
        this.domicilio = domicilio;
        this.numeroEmpleados = numeroEmpleados;
        this.entidad = entidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public int getNumeroEmpleados() {
        return numeroEmpleados;
    }

    public void setNumeroEmpleados(int numeroEmpleados) {
        this.numeroEmpleados = numeroEmpleados;
    }

    public EntidadBancaria getEntidad() {
        return entidad;
    }

    public void setEntidad(EntidadBancaria entidad) {
        this.entidad = entidad;
    }

    @Override
    public String toString() {
        return "Sucursal ["
                + "Código: " + codigo
                + ", Domicilio: " + domicilio
                + ", Empleados: " + numeroEmpleados
                + ", Entidad: " + entidad.getCodigo()
                + "]";
    }
}