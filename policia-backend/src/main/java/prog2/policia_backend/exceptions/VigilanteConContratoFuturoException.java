package prog2.policia_backend.exceptions;

public class VigilanteConContratoFuturoException extends RuntimeException {

    public VigilanteConContratoFuturoException() {
        super("No se puede dar de baja al vigilante porque tiene contratos futuros activos");
    }
}