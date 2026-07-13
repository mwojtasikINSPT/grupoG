/**
 * Contiene las entidades del dominio que representan los objetos de negocio del sistema.
 * <p>Este paquete define la estructura de datos fundamental, incluyendo usuarios, 
 * roles, asaltos, bandas, contratos y entidades bancarias. Estas clases actúan como 
 * la representación en memoria del modelo de información del sistema de policía.</p>
 * * <p><strong>Categorías de modelos:</strong></p>
 * <ul>
 * <li><strong>Usuarios y Roles:</strong> {@link models.Usuario}, {@link models.UsuarioAdministrador}, 
 * {@link models.UsuarioInvestigador}, {@link models.UsuarioVigilante}, {@link models.Rol}</li>
 * <li><strong>Operaciones Judiciales y Delitos:</strong> {@link models.Asalto}, {@link models.Banda}, 
 * {@link models.CasoJudicial}, {@link models.Juez}, {@link models.Asaltante}</li>
 * <li><strong>Infraestructura Bancaria:</strong> {@link models.ContratoVigilancia}, {@link models.Sucursal}, 
 * {@link models.EntidadBancaria}, {@link models.Vigilante}</li>
 * </ul>
 */
package models;