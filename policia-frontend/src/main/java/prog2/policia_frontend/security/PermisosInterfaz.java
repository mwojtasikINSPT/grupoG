package prog2.policia_frontend.security;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;

import org.springframework.stereotype.Component;

import prog2.policia_frontend.views.AsaltantesView;
import prog2.policia_frontend.views.AsaltosView;
import prog2.policia_frontend.views.BandasView;
import prog2.policia_frontend.views.CasosJudicialesView;
import prog2.policia_frontend.views.ContratosVigilanciaView;
import prog2.policia_frontend.views.EntidadesBancariasView;
import prog2.policia_frontend.views.InicioView;
import prog2.policia_frontend.views.JuecesView;
import prog2.policia_frontend.views.MiPerfilView;
import prog2.policia_frontend.views.MisContratosView;
import prog2.policia_frontend.views.RolesView;
import prog2.policia_frontend.views.SucursalesView;
import prog2.policia_frontend.views.UsuariosView;

// Centraliza las consultas de permisos y el acceso a las vistas.
@Component
@VaadinSessionScope
public class PermisosInterfaz {

    private final SesionUsuario sesionUsuario;

    public PermisosInterfaz(SesionUsuario sesionUsuario) {
        this.sesionUsuario = sesionUsuario;
    }

    public boolean puedeLeer(String entidad) {
        return sesionUsuario.tienePermiso(PermisosApp.LECTURA + "_" + entidad);
    }

    public boolean puedeEscribir(String entidad) {
        return sesionUsuario.tienePermiso(PermisosApp.ESCRITURA + "_" + entidad);
    }

    public boolean puedeLeerDatosPropios(String entidad) {
        return puedeLeer(entidad) || sesionUsuario.tienePermiso(PermisosApp.LECTURA_DATOS_PROPIOS + "_" + entidad);
    }

    public boolean puedeAccederVista(Class<?> vista) {
        if (!sesionUsuario.estaAutenticado() || vista == null) {
            return false;
        }
        if (vista.equals(InicioView.class) || vista.equals(MiPerfilView.class)) {
            return true;
        }
        if (vista.equals(UsuariosView.class)) {
            return puedeLeerDatosPropios("USUARIO");
        }
        if (vista.equals(JuecesView.class)) {
            return puedeLeer("JUEZ");
        }
        if (vista.equals(EntidadesBancariasView.class)) {
            return puedeLeer("ENTIDAD_BANCARIA");
        }
        if (vista.equals(SucursalesView.class)) {
            return puedeLeer("SUCURSAL");
        }
        if (vista.equals(ContratosVigilanciaView.class)) {
            return puedeLeer("CONTRATO_VIGILANCIA");
        }
        if (vista.equals(MisContratosView.class)) {
            return puedeLeerDatosPropios("CONTRATO_VIGILANCIA");
        }
        if (vista.equals(BandasView.class)) {
            return puedeLeer("BANDA");
        }
        if (vista.equals(AsaltantesView.class)) {
            return puedeLeer("ASALTANTE");
        }
        if (vista.equals(AsaltosView.class)) {
            return puedeLeer("ASALTO");
        }
        if (vista.equals(CasosJudicialesView.class)) {
            return puedeLeer("CASO_JUDICIAL");
        }
        if (vista.equals(RolesView.class)) {
            return puedeLeer("ROL");
        }
        return false;
    }

    public boolean puedeConsultarAsaltantes() {
        return puedeLeer("ASALTANTE");
    }

    public boolean puedeConsultarSucursales() {
        return puedeLeer("SUCURSAL");
    }

    public boolean puedeModificarAsaltos() {
        return puedeEscribir("ASALTO");
    }

    public boolean puedeConsultarJueces() {
        return puedeLeer("JUEZ");
    }

    public boolean puedeConsultarSentencias() {
        return puedeLeer("SENTENCIA_ASALTANTE");
    }

    public boolean puedeModificarSentencias() {
        return puedeEscribir("SENTENCIA_ASALTANTE");
    }

    public boolean puedeModificarCasos() {
        return puedeEscribir("CASO_JUDICIAL");
    }

    public boolean puedeAdministrarRoles() {
        return puedeEscribir("ROL");
    }

    public boolean puedeModificarBandas() {
        return puedeEscribir("BANDA");
    }

    public boolean puedeModificarJueces() {
        return puedeEscribir("JUEZ");
    }

    public boolean puedeConsultarCasosJudiciales() {
        return puedeLeer("CASO_JUDICIAL");
    }

    public boolean puedeModificarEntidadesBancarias() {
        return puedeEscribir("ENTIDAD_BANCARIA");
    }

    public boolean puedeModificarAsaltantes() {
        return puedeEscribir("ASALTANTE");
    }

    public boolean puedeConsultarBandas() {
        return puedeLeer("BANDA");
    }

    public boolean puedeModificarSucursales() {
        return puedeEscribir("SUCURSAL");
    }

    public boolean puedeConsultarAsaltos() {
        return puedeLeer("ASALTO");
    }

    public boolean puedeConsultarEntidadesBancarias() {
        return puedeLeer("ENTIDAD_BANCARIA");
    }

    public boolean puedeModificarContratos() {
        return puedeEscribir("CONTRATO_VIGILANCIA");
    }

    public boolean puedeConsultarUsuarios() {
        return puedeLeer("USUARIO");
    }

    public boolean puedeModificarUsuarios() {
        return puedeEscribir("USUARIO");
    }
}
