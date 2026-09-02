
package daos;

import models.ContratoVigilancia;

/**
 * Define las operaciones disponibles para administrar contratos de vigilancia.
 *
 * @author Santo
 */
public interface IContratoVigilanciaDAO
        extends IGenericDAO<ContratoVigilancia> {

    /**
     * Comprueba si existe un contrato con el identificador indicado.
     *
     * @param id identificador compuesto del contrato
     * @return {@code true} si el contrato existe; de lo contrario,
     *         {@code false}
     */
    boolean existe(String id);
}