package prog2.policia_backend.exceptions;

public class JuezConCasosJudicialesException extends RuntimeException {

    public JuezConCasosJudicialesException() {
        super("No se puede dar de baja el juez porque tiene casos judiciales asociados");
    }
}
