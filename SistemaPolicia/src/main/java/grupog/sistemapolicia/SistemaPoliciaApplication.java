package grupog.sistemapolicia;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles.css")
@ColorScheme(ColorScheme.Value.DARK)
public class SistemaPoliciaApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(SistemaPoliciaApplication.class, args);
	}

}
