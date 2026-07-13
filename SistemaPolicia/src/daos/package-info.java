/**
 * Proporciona las clases encargadas de la persistencia de datos del sistema.
 * <p>Este paquete encapsula la lógica de acceso a archivos (DAO - Data Access Objects), 
 * abstrayendo los detalles técnicos de lectura y escritura de la información 
 * para que el resto de la aplicación no necesite conocer cómo se almacenan los datos físicamente.</p>
 * <p>Las clases principales incluidas en este paquete son:</p>
 * <ul>
 * <li>{@link daos.AsaltoDAO}</li>
 * <li>{@link daos.BandaDAO}</li>
 * <li>{@link daos.CasoJudicialDAO}</li>
 * <li>{@link daos.ContratoVigilanciaDAO}</li>
 * <li>{@link daos.JuezDAO}</li>
 * <li>{@link daos.SucursalDAO}</li>
 * <li>{@link daos.UsuarioDAO}</li>
 * <li>{@link daos.VigilanteDAO}</li>
 * </ul>
 * * @see daos.DAO
 */
package daos;