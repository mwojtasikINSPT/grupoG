/**
 * Contiene las interfaces de usuario encargadas de la interacción con el sistema.
 * <p>Este paquete gestiona el flujo de navegación de la aplicación, mostrando 
 * menús principales, recibiendo las entradas del usuario y delegando las 
 * operaciones de negocio a los controladores correspondientes. Incluye utilidades 
 * para el manejo de consola y submenús especializados.</p>
 * * <p><strong>Componentes principales:</strong></p>
 * <ul>
 * <li>{@link views.Main} - Punto de entrada del sistema y gestión del login.</li>
 * <li>{@link views.UIHelper} - Herramientas utilitarias para E/S en consola.</li>
 * <li>{@link views.MenuAdministrador}, {@link views.MenuInvestigador}, {@link views.MenuVigilante} - Vistas principales según rol.</li>
 * <li>{@link views.submenues} - Paquete que contiene las interfaces específicas de gestión operativa, judicial y de usuarios.</li>
 * </ul>
 */
package views;