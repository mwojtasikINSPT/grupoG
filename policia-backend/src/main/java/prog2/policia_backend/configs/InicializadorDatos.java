package prog2.policia_backend.configs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.datos.inicializar", havingValue = "true")
@RequiredArgsConstructor
public class InicializadorDatos implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //System.out.println(">>> INICIALIZADOR DE DATOS EJECUTADO");
        try (Connection conexion = dataSource.getConnection()) {
            if (existenDatos(conexion)) {
                return;
            }
        }

        ResourceDatabasePopulator inicializador = new ResourceDatabasePopulator();
        inicializador.addScript(new ClassPathResource("database/data.sql"));
        inicializador.execute(dataSource);
    }

    private boolean existenDatos(Connection conexion) throws Exception {
        String sql = "SELECT COUNT(*) FROM rol";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            resultado.next();
            return resultado.getLong(1) > 0;
        }
    }
}