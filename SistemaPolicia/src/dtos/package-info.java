/**
 * Contiene los objetos de transferencia de datos (DTOs) utilizados en el sistema.
 * <p>Este paquete agrupa las clases que sirven como contenedores de información 
 * para el intercambio de datos entre la capa de presentación (vistas) y la 
 * capa de control. Los DTOs permiten encapsular múltiples valores en un solo 
 * objeto, facilitando la comunicación de parámetros de entrada sin exponer 
 * la estructura interna de los modelos de dominio.</p>
 * <p>Los DTOs definidos en este paquete son:</p>
 * <ul>
 * <li>{@link dtos.DetalleDelitoDTO}</li>
 * <li>{@link dtos.HistorialAsaltanteDTO}</li>
 * <li>{@link dtos.ReporteSucursalDTO}</li>
 * <li>{@link dtos.UsuarioLoginDTO}</li>
 * <li>{@link dtos.VigilanteAsignadoDTO}</li>
 * <li>{@link dtos.VigilanteContratoDTO}</li>
 * </ul>
 * * <p>A diferencia de las clases en {@link models}, los DTOs aquí contenidos 
 * no poseen lógica de negocio ni persistencia, actuando exclusivamente como 
 * estructuras de datos para el transporte.</p>
 */
package dtos;