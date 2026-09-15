package grupog.sistemapolicia.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import grupog.sistemapolicia.dto.UsuarioResponse;
import grupog.sistemapolicia.model.*;
import grupog.sistemapolicia.service.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VistasTest {

    private MockedStatic<Notification> avisos;

    @BeforeEach
    void prepararUI() {
        // Los formularios se prueban desconectados de un navegador real.
        avisos = mockStatic(Notification.class);
        avisos.when(() -> Notification.show(anyString()))
                .thenAnswer(invocacion -> new Notification());
    }

    @AfterEach
    void limpiarUI() {
        avisos.close();
    }

    @Test
    void bandasEnviaDatosActualizaTablaYLimpiaFormulario() {
        BandaService service = mock(BandaService.class);
        Banda banda = new Banda("B-001", 3);
        when(service.listar()).thenReturn(List.of(), List.of(banda));
        BandasView vista = agregar(new BandasView(service));
        texto(vista, "Número de banda").setValue(" B-001 ");
        entero(vista, "Cantidad de miembros").setValue(3);
        registrar(vista);

        ArgumentCaptor<Banda> datos = ArgumentCaptor.forClass(Banda.class);
        verify(service).guardar(datos.capture());
        assertEquals("B-001", datos.getValue().getNumeroBanda());
        assertEquals(3, datos.getValue().getCantMiembros());
        assertTrue(texto(vista, "Número de banda").isEmpty());
        assertEquals(1, tabla(vista).getListDataView().getItemCount());
    }

    @Test
    void cantidadVaciaNoSeGuardaYErroresConservanElFormulario() {
        BandaService service = mock(BandaService.class);
        when(service.listar()).thenReturn(List.of());
        BandasView vista = agregar(new BandasView(service));
        texto(vista, "Número de banda").setValue("B-001");
        registrar(vista);
        verify(service, never()).guardar(any());

        entero(vista, "Cantidad de miembros").setValue(2);
        doThrow(new IllegalArgumentException("La banda ya existe"))
                .when(service).guardar(any());
        registrar(vista);
        assertEquals("B-001", texto(vista, "Número de banda").getValue());
        assertEquals(2, entero(vista, "Cantidad de miembros").getValue());
        verify(service, times(1)).listar();
    }

    @Test
    void juecesEnviaNombreClaveYAnios() {
        JuezService service = mock(JuezService.class);
        when(service.listar()).thenReturn(List.of());
        JuecesView vista = agregar(new JuecesView(service));
        texto(vista, "Clave interna").setValue("J-001");
        texto(vista, "Nombre").setValue(" Juez de prueba ");
        entero(vista, "Años de servicio").setValue(0);
        registrar(vista);
        ArgumentCaptor<Juez> datos = ArgumentCaptor.forClass(Juez.class);
        verify(service).guardar(datos.capture());
        assertEquals("J-001", datos.getValue().getClaveInterna());
        assertEquals("Juez de prueba", datos.getValue().getNombre());
        assertEquals(0, datos.getValue().getAniosServicio());
    }

    @Test
    void asaltantesEnviaLaBandaSeleccionada() {
        AsaltanteService service = mock(AsaltanteService.class);
        BandaService bandas = mock(BandaService.class);
        Banda banda = new Banda("B-001", 3);
        when(service.listar()).thenReturn(List.of());
        when(bandas.listar()).thenReturn(List.of(banda));
        AsaltantesView vista = agregar(new AsaltantesView(service, bandas));
        texto(vista, "Clave").setValue("A-001");
        texto(vista, "Nombre completo").setValue("Persona de prueba");
        VistasTest.<Banda>selector(vista, "Banda").setValue(banda);
        registrar(vista);
        ArgumentCaptor<Asaltante> datos = ArgumentCaptor.forClass(Asaltante.class);
        verify(service).guardar(datos.capture());
        assertEquals("A-001", datos.getValue().getClave());
        assertSame(banda, datos.getValue().getBanda());
    }

    @Test
    void asaltosEnviaLasRelacionesYLaFecha() {
        AsaltoService service = mock(AsaltoService.class);
        AsaltanteService asaltantes = mock(AsaltanteService.class);
        SucursalService sucursales = mock(SucursalService.class);
        Asaltante persona = new Asaltante("A-001", "Prueba", new Banda("B-001", 3));
        Sucursal sucursal = sucursal();
        when(service.listar()).thenReturn(List.of());
        when(asaltantes.listar()).thenReturn(List.of(persona));
        when(sucursales.listar()).thenReturn(List.of(sucursal));
        AsaltosView vista = agregar(new AsaltosView(service, asaltantes, sucursales));
        texto(vista, "Identificador del asalto").setValue("AS-001");
        VistasTest.<Asaltante>selector(vista, "Asaltante").setValue(persona);
        VistasTest.<Sucursal>selector(vista, "Sucursal").setValue(sucursal);
        campo(vista, DatePicker.class, "Fecha").setValue(LocalDate.of(2026, 9, 15));
        registrar(vista);
        ArgumentCaptor<Asalto> datos = ArgumentCaptor.forClass(Asalto.class);
        verify(service).guardar(datos.capture());
        assertSame(persona, datos.getValue().getAsaltante());
        assertSame(sucursal, datos.getValue().getSucursal());
        assertEquals(LocalDate.of(2026, 9, 15), datos.getValue().getFecha());
    }

    @Test
    void contratosEnviaSucursalVigilanteFechaYArma() {
        ContratoVigilanciaService service = mock(ContratoVigilanciaService.class);
        SucursalService sucursales = mock(SucursalService.class);
        VigilanteService vigilantes = mock(VigilanteService.class);
        Sucursal sucursal = sucursal();
        Vigilante vigilante = new Vigilante("V-001", 30);
        when(service.listar()).thenReturn(List.of());
        when(sucursales.listar()).thenReturn(List.of(sucursal));
        when(vigilantes.listar()).thenReturn(List.of(vigilante));
        ContratosVigilanciaView vista = agregar(
                new ContratosVigilanciaView(service, sucursales, vigilantes));
        texto(vista, "Código").setValue("C-001");
        VistasTest.<Sucursal>selector(vista, "Sucursal").setValue(sucursal);
        VistasTest.<Vigilante>selector(vista, "Vigilante").setValue(vigilante);
        campo(vista, DatePicker.class, "Fecha").setValue(LocalDate.of(2026, 9, 15));
        campo(vista, Checkbox.class, "Con arma").setValue(true);
        registrar(vista);
        ArgumentCaptor<ContratoVigilancia> datos =
                ArgumentCaptor.forClass(ContratoVigilancia.class);
        verify(service).guardar(datos.capture());
        assertEquals("C-001", datos.getValue().getCodigo());
        assertSame(sucursal, datos.getValue().getSucursal());
        assertSame(vigilante, datos.getValue().getVigilante());
        assertTrue(datos.getValue().isConArma());
        assertFalse(campo(vista, Checkbox.class, "Con arma").getValue());
    }

    @Test
    void condenaRequiereMesesYEnviaLaCantidad() {
        CasoJudicialService service = mock(CasoJudicialService.class);
        CasosJudicialesView vista = casoVista(service);
        campo(vista, Checkbox.class, "Condenado").setValue(true);
        assertTrue(entero(vista, "Meses de cárcel").isEnabled());
        registrar(vista);
        verify(service, never()).guardar(any());
        entero(vista, "Meses de cárcel").setValue(12);
        registrar(vista);
        ArgumentCaptor<CasoJudicial> datos = ArgumentCaptor.forClass(CasoJudicial.class);
        verify(service).guardar(datos.capture());
        assertTrue(datos.getValue().isCondenado());
        assertEquals(12, datos.getValue().getMesesCarcel());
        assertEquals("AS-001", datos.getValue().getIdAsalto());
        assertFalse(entero(vista, "Meses de cárcel").isEnabled());
    }

    @Test
    void casoSinCondenaGuardaCeroMesesYFiltroConsultaDetenidos() {
        CasoJudicialService service = mock(CasoJudicialService.class);
        CasosJudicialesView vista = casoVista(service);
        registrar(vista);
        ArgumentCaptor<CasoJudicial> datos = ArgumentCaptor.forClass(CasoJudicial.class);
        verify(service).guardar(datos.capture());
        assertFalse(datos.getValue().isCondenado());
        assertEquals(0, datos.getValue().getMesesCarcel());
        campo(vista, Checkbox.class, "Mostrar sólo condenados").setValue(true);
        verify(service).listarDetenidos();
    }

    @Test
    void usuariosEsSoloLecturaYUsaRespuestaSinPassword() {
        UsuarioService service = mock(UsuarioService.class);
        when(service.listar()).thenReturn(List.of(
                new UsuarioResponse("prueba", Rol.INVESTIGADOR, null)));
        UsuariosView vista = agregar(new UsuariosView(service));
        assertEquals(3, tabla(vista).getColumns().size());
        assertEquals(1, tabla(vista).getListDataView().getItemCount());
        buscar(vista, Button.class, boton ->
                "Actualizar listado".equals(boton.getText())).click();
        verify(service, times(2)).listar();
        verify(service, never()).guardar(any());
    }

    @Test
    void rutasSonUnicasYUsanElMenuPrincipal() {
        Map<Class<?>, String> rutas = Map.of(
                BandasView.class, "bandas", JuecesView.class, "jueces",
                AsaltantesView.class, "asaltantes", AsaltosView.class, "asaltos",
                ContratosVigilanciaView.class, "contratos-vigilancia",
                CasosJudicialesView.class, "casos-judiciales",
                UsuariosView.class, "usuarios");
        rutas.forEach((tipo, direccion) -> {
            Route ruta = tipo.getAnnotation(Route.class);
            assertEquals(direccion, ruta.value());
            assertEquals(MainLayout.class, ruta.layout());
        });
    }

    private static Sucursal sucursal() {
        return new Sucursal("S-001", "Domicilio de prueba", 10,
                new EntidadBancaria("E-001", "Central de prueba"));
    }

    private static CasosJudicialesView casoVista(CasoJudicialService service) {
        AsaltoService asaltos = mock(AsaltoService.class);
        JuezService jueces = mock(JuezService.class);
        Asalto asalto = new Asalto("AS-001", null, sucursal(), LocalDate.of(2026, 9, 15));
        Juez juez = new Juez("J-001", 5, "Juez de prueba");
        when(service.listar()).thenReturn(List.of());
        when(service.listarDetenidos()).thenReturn(List.of());
        when(asaltos.listar()).thenReturn(List.of(asalto));
        when(jueces.listar()).thenReturn(List.of(juez));
        CasosJudicialesView vista = agregar(new CasosJudicialesView(service, asaltos, jueces));
        VistasTest.<Asalto>selector(vista, "Asalto").setValue(asalto);
        VistasTest.<Juez>selector(vista, "Juez").setValue(juez);
        return vista;
    }

    private static <T extends Component> T agregar(T vista) {
        return vista;
    }

    private static TextField texto(Component vista, String etiqueta) {
        return campo(vista, TextField.class, etiqueta);
    }

    private static IntegerField entero(Component vista, String etiqueta) {
        return campo(vista, IntegerField.class, etiqueta);
    }

    @SuppressWarnings("unchecked")
    private static <T> ComboBox<T> selector(Component vista, String etiqueta) {
        return campo(vista, ComboBox.class, etiqueta);
    }

    private static Grid<?> tabla(Component vista) {
        return buscar(vista, Grid.class, valor -> true);
    }

    private static void registrar(Component vista) {
        buscar(vista, Button.class, boton -> "Registrar".equals(boton.getText())).click();
    }

    private static <T extends Component> T campo(Component vista,
            Class<T> tipo, String etiqueta) {
        return buscar(vista, tipo, valor -> valor instanceof HasLabel campo
                && etiqueta.equals(campo.getLabel()));
    }

    private static <T extends Component> T buscar(Component vista,
            Class<T> tipo, Predicate<T> condicion) {
        return componentes(vista).filter(tipo::isInstance).map(tipo::cast)
                .filter(condicion).findFirst().orElseThrow();
    }

    private static Stream<Component> componentes(Component raiz) {
        return Stream.concat(Stream.of(raiz),
                raiz.getChildren().flatMap(VistasTest::componentes));
    }
}
