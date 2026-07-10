package views;

// Clase utilitaria para manejar la E/S por consola (Scanner, validaciones).

import java.util.Scanner;

public class UIHelper {

    private static final Scanner teclado = new Scanner(System.in);
   
    //Mostrar titulo
    public static void mostrarTitulo(String titulo){
        System.out.println("\n========================================");
        System.out.println(" " + titulo.toUpperCase());
        System.out.println("\n========================================");
    }
    
    //Lee un texto obligatorio (no permite vacíos)
    public static String leerTexto(String mensaje){
        String entrada;
        do{
            System.out.println(mensaje + ": ");
            entrada = teclado.nextLine().trim();
            if(entrada.isEmpty()){
                System.out.println("El campo no puede estar vacío.");
            }
        }while(entrada.isEmpty());
        return entrada;
    }
    
    //Lee un número entero valido
    public static int leerEntero(String mensaje){
        while(true){
            try{
                System.out.println(mensaje + ": ");
                return Integer.parseInt(teclado.nextLine().trim());
            }catch(NumberFormatException e){
                System.out.println("Debe ingresar un número entero válido.");
            }
        }
    }
    
    //Leer una opción booleana
    public static boolean leerBooleano(String mensaje){
        while(true){
            System.out.println(mensaje + "(S/N): ");
            String entrada = teclado.nextLine().trim().toUpperCase();
            if(entrada.equals("S")) return true;
            if(entrada.equals("N")) return false;
            System.out.println("Opción inválida. Ingrese 'S' para Sí o 'N' para No.");
        }
    }
}
