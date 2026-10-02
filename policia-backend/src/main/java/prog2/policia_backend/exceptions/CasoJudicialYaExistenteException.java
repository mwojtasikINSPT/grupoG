package prog2.policia_backend.exceptions;

public class CasoJudicialYaExistenteException extends RuntimeException {

    public CasoJudicialYaExistenteException() {
        super("Ya existe un caso judicial para este asalto y asaltante");
    }
}