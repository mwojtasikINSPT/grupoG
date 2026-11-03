package prog2.policia_frontend.dtos;

public class RolPermisoEntidadDTO {
    private String permiso;
    private String entidad;

    public RolPermisoEntidadDTO() {}
    public RolPermisoEntidadDTO(String permiso, String entidad) { this.permiso = permiso; this.entidad = entidad; }
    public String getPermiso() { return permiso; }
    public void setPermiso(String permiso) { this.permiso = permiso; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
}
