package views;

import java.util.Scanner;
import models.Rol;

/**
 * Clase utilitaria para manejar la entrada y salida por consola. Centraliza las
 * operaciones de impresión de mensajes, lectura de datos y validaciones
 * básicas, proporcionando una interfaz uniforme para la interacción con el
 * usuario.
 *
 * Funcionalidades principales: - Mostrar títulos y subtítulos formateados en
 * consola. - Leer texto, números enteros, valores booleanos y roles válidos. -
 * Imprimir mensajes informativos, de error y de éxito. - Pausar la ejecución
 * hasta que el usuario presione ENTER. - Limpiar la pantalla según el sistema
 * operativo.
 *
 * Esta clase es utilizada por los distintos menús y submenues del sistema para
 * garantizar una experiencia de usuario consistente y validada.
 *
 * @author GrupoG
 */
public class UIHelper {

    private static final Scanner teclado = new Scanner(System.in);

    /**
     * Muestra un título principal centrado y formateado.
     *
     * @param titulo El texto del título a mostrar.
     */
    public static void mostrarTitulo(String titulo) {
        // Defino un margen de espacios para el centrado
        String margen = "    ";

        System.out.println("\n########################################");
        System.out.println(margen + titulo.toUpperCase());
        System.out.println("########################################\n");
    }

    /**
     * Muestra un subtítulo formateado en consola.
     *
     * @param subtitulo El texto del subtítulo a mostrar.
     */
    public static void mostrarSubtitulo(String subtitulo) {
        System.out.println("\n========================================");
        System.out.println(subtitulo.toUpperCase());
        System.out.println("========================================");
    }

    /**
     * Lee un texto del usuario, asegurando que no sea una cadena vacía.
     *
     * @param mensaje El texto que guía al usuario sobre qué debe ingresar.
     * @return El texto ingresado.
     */
    public static String leerTexto(String mensaje) {
        String entrada;
        do {
            System.out.println(mensaje + ": ");
            entrada = teclado.nextLine().trim();
            if (entrada.isEmpty()) {
                System.out.println("El campo no puede estar vacío.");
            }
        } while (entrada.isEmpty());
        return entrada;
    }

    /**
     * Lee un número entero del usuario, capturando errores de formato.
     *
     * @param mensaje El texto que guía al usuario.
     * @return El número entero ingresado.
     */
    public static int leerEntero(String mensaje) {
        while (true) {
            try {
                System.out.println(mensaje + ": ");
                // Usamos nextLine para consumir todo, incluido el \n, sino fallaba en Pausar
                return Integer.parseInt(teclado.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un número entero válido.");
            }
        }
    }

    /**
     * Solicita una confirmación booleana (S/N) al usuario.
     *
     * @param mensaje El texto que guía al usuario.
     * @return true si el usuario ingresa 'S', false si ingresa 'N'.
     */
    public static boolean leerBooleano(String mensaje) {
        while (true) {
            System.out.println(mensaje + "(S/N): ");
            String entrada = teclado.nextLine().trim().toUpperCase();
            if (entrada.equals("S")) {
                return true;
            }
            if (entrada.equals("N")) {
                return false;
            }
            System.out.println("Opción inválida. Ingrese 'S' para Sí o 'N' para No.");
        }
    }

    /**
     * Imprime un mensaje genérico en consola.
     *
     * @param mensaje El mensaje a imprimir.
     */
    public static void imprimirMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    /**
     * Imprime un mensaje de error formateado.
     *
     * @param mensaje El texto del error a mostrar.
     */
    public static void imprimirError(String mensaje) {
        System.out.println("\n[ERROR] " + mensaje);
    }

    /**
     * Imprime un mensaje de éxito formateado.
     *
     * @param mensaje El texto del éxito a mostrar.
     */
    public static void imprimirExito(String mensaje) {
        System.out.println("\n[ÉXITO] " + mensaje);
    }

    /**
     * Pausa la ejecución del programa hasta que el usuario presione ENTER.
     */
    public static void pausar() {
        System.out.print("\nPresione ENTER para continuar...");
        teclado.nextLine();
    }

    /**
     * Solicita un rol al usuario y valida que corresponda con una constante del
     * Enum {@link Rol}.
     *
     * @param mensaje El texto de guía para la entrada.
     * @return El objeto {@link Rol} correspondiente.
     */
    public static Rol leerRol(String mensaje) {
        while (true) {
            // Pido el Rol 
            String entrada = leerTexto(mensaje).toUpperCase();

            try {
                // Intento transformar el texto en rol valido del Enum
                return Rol.valueOf(entrada);

            } catch (IllegalArgumentException e) {
                // Si el texto no coincide con ningún Enum, atrapo el error y aviso al admin
                System.out.println("[ERROR] Rol inválido. Escriba exactamente: ADMINISTRADOR, INVESTIGADOR o VIGILANTE.");
            }
        }
    }

    /**
     * Limpia la pantalla de la consola. Intenta ejecutar comandos del sistema;
     * si falla, utiliza una impresión masiva de líneas en blanco.
     */
    public static void limpiarPantalla() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Comando  de Windows para limpiar pantalla
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Comando  de Linux/Mac
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            // Si  falla, imprimimos muchas líneas... es lo que hay x ahora
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
}
