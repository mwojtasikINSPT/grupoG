package models;

/**
 * Define los diferentes perfiles o roles disponibles para los usuarios dentro
 * del sistema.
 */
public enum Rol {
    /**
     * Acceso total a la configuración y gestión del sistema.
     */
    ADMINISTRADOR,
    /**
     * Acceso a consultas sobre delitos, casos judiciales y reportes de
     * seguridad.
     */
    INVESTIGADOR,
    /**
     * Acceso restringido para el registro de eventos y turnos de vigilancia.
     */
    VIGILANTE
}
