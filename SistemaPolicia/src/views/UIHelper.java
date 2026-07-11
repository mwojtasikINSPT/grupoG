package views;

// Clase utilitaria para manejar la E/S por consola (Scanner, validaciones).
import java.util.Scanner;
import models.Rol;

public class UIHelper {

    private static final Scanner teclado = new Scanner(System.in);

    // Mostrar Título principal
    public static void mostrarTitulo(String titulo) {
        // Defino un margen de espacios para el centrado
        String margen = "    ";
        
        System.out.println("\n########################################");
        System.out.println( margen+ titulo.toUpperCase());
        System.out.println("########################################\n");
    }

    public static void mostrarSubtitulo(String subtitulo) {
        System.out.println("\n========================================");
        System.out.println(subtitulo.toUpperCase());
        System.out.println("========================================");
    }

    //Lee un texto obligatorio (no permite vacíos)
    public static String leerTexto(String mensaje) {
        String entrada;
        do {
            System.out.println(mensaje+ ": ");
            entrada = teclado.nextLine().trim();
            if (entrada.isEmpty()) {
                System.out.println("El campo no puede estar vacío.");
            }
        } while (entrada.isEmpty());
        return entrada;
    }

    //Lee un número entero valido
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

    //Leer una opción booleana
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

    // Muestra un mensaje 
    public static void imprimirMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    // Muestra un mensaje de error, despues deberiamos usar exceptions ?
    public static void imprimirError(String mensaje) {
        System.out.println("\n[ERROR] " + mensaje);
    }

    // Muestra un mensaje de ok
    public static void imprimirExito(String mensaje) {
        System.out.println("\n[ÉXITO] " + mensaje);
    }

    // Pausa el sistema hasta que el usuario presione ENTER 
    public static void pausar() {
        System.out.print("\nPresione ENTER para continuar...");
        teclado.nextLine();
    }

    // Leo el texto del usuario y lo convierto obligatoriamente en un Rol válido
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
}
