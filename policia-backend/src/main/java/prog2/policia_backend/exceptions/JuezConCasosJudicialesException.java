package prog2.policia_backend.exceptions;

public class JuezConCasosJudicialesException extends RuntimeException {

    public JuezConCasosJudicialesException() {
        super("El juez tiene casos judiciales asociados, Asignar nuevo");
    }
}
