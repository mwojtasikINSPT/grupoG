
package daos;

import models.Asalto;

/**
 * Define las operaciones de persistencia disponibles para los asaltos.
 *
 * Permite almacenar los hechos en archivos o en MySQL sin modificar los
 * controladores que utilizan esta información.
 *
 * @author GrupoG
 */
public interface IAsaltoDAO extends IGenericDAO<Asalto> {

    /**
     * Comprueba si existe un asalto con el identificador indicado.
     *
     * @param id identificador del asalto
     * @return {@code true} si el asalto existe
     */
    boolean existe(String id);
}