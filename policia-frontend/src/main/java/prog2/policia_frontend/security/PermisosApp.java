package prog2.policia_frontend.security;

import java.util.List;

//Contiene los nombres de los permisos que reconoce el backend
public final class PermisosApp {

    public static final String LECTURA = "LECTURA";

    public static final String ESCRITURA = "ESCRITURA";

    public static final String LECTURA_DATOS_PROPIOS = "LECTURA_DATOS_PROPIOS";

    //Los permisos son fijos, los roles combinan uno o más.
    public static List<String> todos() {
        return List.of(
                LECTURA,
                ESCRITURA,
                LECTURA_DATOS_PROPIOS
        );
    }

    //Impide crear objetos de esta clase,
    //porque solo contiene valores constantes.
    private PermisosApp() {
    }
}
