//DTO para unir los datos del vigilante con la portacion o no de arma al ser asignado

package dtos;


public class VigilanteAsignadoDTO {
    private String codigoVigilante;
    private int edad;
    private String portacionArma; //SI / NO

    public VigilanteAsignadoDTO(String codigoVigilante, int edad, String portacionArma) {
        this.codigoVigilante = codigoVigilante;
        this.edad = edad;
        this.portacionArma = portacionArma;
    }

    public String getCodigoVigilante() {return codigoVigilante;}

    public int getEdad() {return edad;}

    public String getPortacionArma() {return portacionArma;}
    
    
}
