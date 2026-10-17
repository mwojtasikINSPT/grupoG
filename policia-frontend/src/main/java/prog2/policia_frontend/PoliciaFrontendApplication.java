package prog2.policia_frontend;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.theme.aura.Aura;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StyleSheet(Aura.STYLESHEET)// estilo FORMATO OFICIAL
@StyleSheet("context://styles/police-theme.css") // Colores azules de fondo 
@ColorScheme(ColorScheme.Value.DARK) //ESTE LE DA MODO OSCURO
public class PoliciaFrontendApplication
        implements AppShellConfigurator { // le da el PERMISO visual de la app desde esta clase 

	public static void main(String[] args) {
		SpringApplication.run(PoliciaFrontendApplication.class, args);
	}

}
