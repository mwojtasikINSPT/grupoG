package prog2.policia_frontend.views;

import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import prog2.policia_frontend.clients.EntidadBancariaClient;
import prog2.policia_frontend.components.PaginadorGrid;
import prog2.policia_frontend.dtos.EntidadBancariaDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosInterfaz;
import prog2.policia_frontend.utils.NormalizadorBusqueda;
import prog2.policia_frontend.utils.ErrorBackendUtils;
import prog2.policia_frontend.utils.NotificacionUtils;

// Vista para administrar entidades bancarias.
@Route(value = "entidades-bancarias", layout = MainLayout.class)
@PageTitle("Entidades bancarias | Sistema Policial")
public class EntidadesBancariasView extends VerticalLayout implements BeforeEnterObserver {

    private final EntidadBancariaClient entidadClient;
    private final PermisosInterfaz permisosInterfaz;
    private final Grid<EntidadBancariaDTO> tabla = new Grid<>(EntidadBancariaDTO.class, false);
    private final PaginadorGrid<EntidadBancariaDTO> paginadorEntidades = new PaginadorGrid<>(tabla, 10);

    // Permite buscar por nombre o domicilio.
    private final Select<String> criterioBusqueda = new Select<>();
    private final com.vaadin.flow.component.textfield.TextField valorBusqueda = new com.vaadin.flow.component.textfield.TextField("Buscar");
    private List<EntidadBancariaDTO> datosBusqueda = List.of();
    private final Select<String> filtroEstado = new Select<>();
    private final Button botonNuevaEntidad = new Button("Nueva entidad bancaria");

    public EntidadesBancariasView(EntidadBancariaClient entidadClient, PermisosInterfaz permisosInterfaz) {
        this.entidadClient = entidadClient;
        this.permisosInterfaz = permisosInterfaz;
        configurarFiltros();
        configurarVista();
        configurarTabla();
    }

    // Configura las opciones de búsqueda.
    private void configurarFiltros() {
        configurarBusqueda();
        filtroEstado.setLabel("Estado");
        filtroEstado.setItems("Todos", "Activos", "Inactivos");
        filtroEstado.setValue("Todos");
    }

    // Organiza visualmente la pantalla.
    private void configurarVista() {
        H2 titulo = new H2("Entidades Bancarias");

        HorizontalLayout filtros = crearBarraBusqueda();
        filtros.setAlignItems(Alignment.END);

        botonNuevaEntidad.addClickListener(evento -> abrirFormulario(null));
        botonNuevaEntidad.setVisible(puedeEscribir());

        setSizeFull();
        add(titulo, botonNuevaEntidad, filtros, tabla, paginadorEntidades.getControles());
        expand(tabla);
    }

    // Define las columnas de la tabla.
    private void configurarTabla() {
        tabla.addColumn(EntidadBancariaDTO::getCodigo).setHeader("Código").setAutoWidth(true);
        tabla.addColumn(EntidadBancariaDTO::getNombre).setHeader("Nombre").setAutoWidth(true);
        tabla.addColumn(EntidadBancariaDTO::getDomicilioCentral).setHeader("Domicilio central").setAutoWidth(true).setFlexGrow(1);
        tabla.addColumn(entidad -> Boolean.TRUE.equals(entidad.getActivo()) ? "Activa" : "Inactiva").setHeader("Estado").setAutoWidth(true);
        tabla.addComponentColumn(this::crearBotonesAccion).setHeader("Acciones").setAutoWidth(true);
        tabla.setSizeFull();
    }

    // Permite modificar solamente entidades activas.
    private Component crearBotonesAccion(EntidadBancariaDTO entidad) {
        Button botonVerSucursales = new Button("Ver sucursales", evento -> verSucursales(entidad));
        botonVerSucursales.setVisible(permisosInterfaz.puedeConsultarSucursales());

        if (!puedeEscribir()) {
            return botonVerSucursales;
        }

        boolean estaActiva = Boolean.TRUE.equals(entidad.getActivo());

        Button botonModificar = new Button("Modificar", evento -> abrirFormulario(entidad));
        botonModificar.setEnabled(estaActiva);

        if (!estaActiva) {
            botonModificar.setTooltipText("Debe reactivar la entidad antes de modificarla.");
        }

        Button botonEstado = new Button();

        if (estaActiva) {
            botonEstado.setText("Dar de baja");
            botonEstado.addClickListener(evento -> abrirDialogoBaja(entidad));
        } else {
            botonEstado.setText("Reactivar");
            botonEstado.addClickListener(evento -> reactivarEntidad(entidad));
        }

        return new HorizontalLayout(botonVerSucursales, botonModificar, botonEstado);
    }

    private void verSucursales(EntidadBancariaDTO entidad) {
        UI.getCurrent().navigate(SucursalesView.class, QueryParameters.simple(Map.of("entidad", entidad.getCodigo())));
    }

    // Abre el formulario para registrar o modificar una entidad bancaria.
    private void abrirFormulario(EntidadBancariaDTO entidad) {
        boolean esCreacion = entidad == null;

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle(esCreacion ? "Nueva entidad bancaria" : "Modificar " + entidad.getCodigo());

        TextField campoNombre = new TextField("Nombre");
        TextField campoDomicilio = new TextField("Domicilio central");

        campoNombre.setRequiredIndicatorVisible(true);
        campoDomicilio.setRequiredIndicatorVisible(true);
        campoNombre.setWidthFull();
        campoDomicilio.setWidthFull();

        if (!esCreacion) {
            campoNombre.setValue(entidad.getNombre() != null ? entidad.getNombre() : "");
            campoDomicilio.setValue(entidad.getDomicilioCentral() != null ? entidad.getDomicilioCentral() : "");
        }

        Button botonGuardar = new Button(esCreacion ? "Guardar" : "Guardar cambios");
        Button botonCancelar = new Button("Cancelar", evento -> dialogo.close());

        botonGuardar.addClickListener(evento -> {
            boolean nombreVacio = campoNombre.getValue().isBlank();
            boolean domicilioVacio = campoDomicilio.getValue().isBlank();

            campoNombre.setInvalid(nombreVacio);
            campoNombre.setErrorMessage("El nombre es obligatorio.");
            campoDomicilio.setInvalid(domicilioVacio);
            campoDomicilio.setErrorMessage("El domicilio es obligatorio.");

            if (nombreVacio || domicilioVacio) {
                return;
            }

            EntidadBancariaDTO datos = new EntidadBancariaDTO(campoNombre.getValue().trim(), campoDomicilio.getValue().trim());

            try {
                if (esCreacion) {
                    entidadClient.crear(datos);
                } else {
                    entidadClient.actualizar(entidad.getCodigo(), datos);
                }

                dialogo.close();
                cargarEntidades();
                NotificacionUtils.mostrarExito(esCreacion ? "Entidad bancaria creada correctamente." : "Entidad bancaria modificada correctamente.");

            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            } catch (IllegalArgumentException excepcion) {
                NotificacionUtils.mostrarInformacion(excepcion.getMessage());
            }
        });

        dialogo.add(new VerticalLayout(campoNombre, campoDomicilio, new HorizontalLayout(botonGuardar, botonCancelar)));
        dialogo.open();
    }

    // Solicita el motivo antes de dar de baja una entidad activa.
    private void abrirDialogoBaja(EntidadBancariaDTO entidad) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Dar de baja a " + entidad.getNombre());

        Select<String> campoMotivo = new Select<>();
        campoMotivo.setLabel("Motivo de baja");

        try {
            List<String> motivos = entidadClient.listarMotivosBaja();
            if (motivos.isEmpty()) {
                NotificacionUtils.mostrarInformacion("No hay motivos de baja disponibles.");
                return;
            }
            campoMotivo.setItems(motivos);
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
            return;
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se pueden consultar los motivos de baja.");
            return;
        }

        campoMotivo.setRequiredIndicatorVisible(true);

        Button botonConfirmar = new Button("Confirmar baja");
        Button botonCancelar = new Button("Cancelar", evento -> dialogo.close());

        botonConfirmar.addClickListener(evento -> {
            String motivo = campoMotivo.getValue();

            if (motivo == null) {
                campoMotivo.setInvalid(true);
                campoMotivo.setErrorMessage("Debe seleccionar un motivo.");
                return;
            }

            try {
                entidadClient.darDeBaja(entidad.getCodigo(), motivo);
                dialogo.close();
                cargarEntidades();
                NotificacionUtils.mostrarExito("Entidad bancaria dada de baja correctamente.");

            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            } catch (IllegalArgumentException excepcion) {
                NotificacionUtils.mostrarInformacion(excepcion.getMessage());
            }
        });

        dialogo.add(campoMotivo, new HorizontalLayout(botonConfirmar, botonCancelar));
        dialogo.open();
    }

    // Reactiva una entidad bancaria inactiva.
    private void reactivarEntidad(EntidadBancariaDTO entidad) {
        try {
            entidadClient.reactivar(entidad.getCodigo());
            cargarEntidades();
            NotificacionUtils.mostrarExito("Entidad bancaria reactivada correctamente.");

        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        } catch (IllegalArgumentException excepcion) {
            NotificacionUtils.mostrarInformacion(excepcion.getMessage());
        }
    }

    // Convierte los errores HTTP en mensajes comprensibles.
    private void mostrarErrorOperacion(RestClientResponseException excepcion) {
        NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    // Carga los datos al abrir la vista.
    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        cargarEntidades();
    }

    // Convierte el estado visual al valor esperado por el backend.
    private Boolean obtenerEstado() {
        return switch (filtroEstado.getValue()) {
            case "Activos" ->
                true;
            case "Inactivos" ->
                false;
            default ->
                null;
        };
    }

    // Consulta las entidades bancarias aplicando los filtros seleccionados.
    private void cargarEntidades() {
        datosBusqueda = List.of();
        aplicarFiltros();
        try {
            datosBusqueda = entidadClient.listar(null);
            aplicarFiltros();
        } catch (RestClientResponseException excepcion) {
            datosBusqueda = List.of();
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            datosBusqueda = List.of();
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        } catch (IllegalStateException excepcion) {
            datosBusqueda = List.of();
            NotificacionUtils.mostrarInformacion("Debe iniciar sesión.");
        }
    }

    private boolean puedeEscribir() {
        return permisosInterfaz.puedeModificarEntidadesBancarias();
    }

    private void configurarBusqueda() {
        criterioBusqueda.setLabel("Criterio de búsqueda");
        criterioBusqueda.setItems("Todas", "Código", "Nombre", "Domicilio", "Motivo de baja");
        criterioBusqueda.setValue("Todas");
        valorBusqueda.setPlaceholder("Ingrese una coincidencia");
        valorBusqueda.setClearButtonVisible(true);
        valorBusqueda.setVisible(false);
        criterioBusqueda.addValueChangeListener(new com.vaadin.flow.component.HasValue.ValueChangeListener<com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<Select<String>, String>>() {
            @Override public void valueChanged(com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<Select<String>, String> evento) {
                valorBusqueda.clear();
                valorBusqueda.setVisible(!"Todas".equals(evento.getValue()));
                valorBusqueda.setLabel(evento.getValue());
            }
        });
    }

    private HorizontalLayout crearBarraBusqueda() {
        Button buscar = new Button("Buscar", new com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>>() {
            @Override public void onComponentEvent(com.vaadin.flow.component.ClickEvent<Button> evento) { aplicarFiltros(); }
        });
        Button limpiar = new Button("Limpiar", new com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>>() {
            @Override public void onComponentEvent(com.vaadin.flow.component.ClickEvent<Button> evento) {
                criterioBusqueda.setValue("Todas");
                valorBusqueda.clear();
                filtroEstado.setValue("Todos");
                aplicarFiltros();
            }
        });
        HorizontalLayout barra = new HorizontalLayout(criterioBusqueda, valorBusqueda, filtroEstado, buscar, limpiar);
        barra.setAlignItems(HorizontalLayout.Alignment.END);
        return barra;
    }

    private void aplicarFiltros() {
        String criterio = criterioBusqueda.getValue();
        String texto = NormalizadorBusqueda.normalizar(valorBusqueda.getValue());
        List<EntidadBancariaDTO> filtrados = new java.util.ArrayList<>();
        for (EntidadBancariaDTO dato : datosBusqueda) {
            if ((obtenerEstado() == null || obtenerEstado().equals(dato.getActivo())) && coincideCriterio(dato, criterio, texto)) {
                filtrados.add(dato);
            }
        }
        paginadorEntidades.setItems(filtrados);
    }

    private boolean coincideCriterio(EntidadBancariaDTO dato, String criterio, String texto) {
        if ("Código".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getCodigo()).contains(texto); }
        if ("Nombre".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getNombre()).contains(texto); }
        if ("Domicilio".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getDomicilioCentral()).contains(texto); }
        if ("Motivo de baja".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getMotivoBaja()).contains(texto); }
        return true;
    }

}
