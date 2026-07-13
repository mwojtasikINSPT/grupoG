/**
 * Contiene las excepciones personalizadas utilizadas para gestionar los errores
 * específicos del dominio de la aplicación.
 * <p>Este paquete centraliza el manejo de fallos durante las operaciones de
 * persistencia y lógica de negocio, permitiendo que las capas superiores
 * (controladores y vistas) identifiquen y respondan adecuadamente a situaciones
 * excepcionales como fallos de lectura/escritura o registros no encontrados.</p>
 * <p>Excepciones principales:</p>
 * <ul>
 * <li>{@link exceptions.ErrorAlActualizarException}</li>
 * <li>{@link exceptions.ErrorAlEliminarException}</li>
 * <li>{@link exceptions.ErrorAlGuardarException}</li>
 * <li>{@link exceptions.ErrorAlLeerException}</li>
 * <li>{@link exceptions.ObjetoNoEncontradoException}</li>
 * </ul>
 */
package exceptions;