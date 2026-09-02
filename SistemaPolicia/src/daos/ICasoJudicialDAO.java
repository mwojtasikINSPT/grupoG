package daos;

import models.CasoJudicial;

/**
 * Define las operaciones de persistencia disponibles para los casos
 * judiciales.
 *
 * Permite almacenar los casos en archivos o en MySQL sin modificar los
 * controladores que utilizan esta información.
 *
 * @author GrupoG
 */
public interface ICasoJudicialDAO
        extends IGenericDAO<CasoJudicial> {

    /**
     * Comprueba si ya existe un caso para el asalto indicado.
     *
     * @param idAsalto identificador del asalto
     * @return {@code true} si existe un caso asociado
     */
    boolean existe(String idAsalto);
}