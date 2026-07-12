package daos;

import exceptions.*;
import java.util.List;

/**
 * Interfaz generica para operaciones CRUD.
 * @param <T> Tipo de objeto gestionado.
 */
public interface IGenericDAO<T> {
    
    /**
     * Guarda una entidad.
     * @param entidad Objeto a guardar.
     * @throws ErrorAlGuardarException Error en persistencia.
     */
    void guardar(T entidad) throws ErrorAlGuardarException;
    
    /**
     * Obtiene todos los registros.
     * @return Lista de objetos.
     * @throws ErrorAlLeerException Error en lectura.
     */
    List<T> obtenerTodos() throws ErrorAlLeerException;
    
    /**
     * Busca por ID.
     * @param id Identificador.
     * @return Objeto encontrado.
     * @throws ObjetoNoEncontradoException Si no existe.
     * @throws ErrorAlLeerException Error en lectura.
     */
    T buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException;
    
    /**
     * Actualiza una entidad.
     * @param entidad Objeto con cambios.
     * @throws ErrorAlActualizarException Error al actualizar.
     */
    void actualizar(T entidad) throws ErrorAlActualizarException;
    
    /**
     * Elimina por ID.
     * @param id Identificador.
     * @throws ErrorAlEliminarException Error al eliminar.
     */
    void eliminar(String id) throws ErrorAlEliminarException;
}