package daos;

import exceptions.ErrorAlActualizarException;
import exceptions.ErrorAlEliminarException;
import exceptions.ErrorAlGuardarException;
import exceptions.ErrorAlLeerException;
import exceptions.ObjetoNoEncontradoException;
import java.util.List;

// Interfaz genérica con métodos: crear(), leer(), actualizar(), eliminar() para todos los DAO.
public interface IGenericDAO<T> {
    
    // C - Create (Crear/Guardar)
    void guardar(T entidad) throws ErrorAlGuardarException;
    
    // R - Read (Leer)
    List<T> obtenerTodos() throws ErrorAlLeerException;
    T buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException;
    
    // U - Update (Actualizar)
    void actualizar(T entidad) throws ErrorAlActualizarException;
    
    // D - Delete (Eliminar)
    void eliminar(String id) throws ErrorAlEliminarException;
}
