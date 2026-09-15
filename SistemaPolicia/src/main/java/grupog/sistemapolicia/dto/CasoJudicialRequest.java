package grupog.sistemapolicia.dto;

public record CasoJudicialRequest(
        String idAsalto,
        String claveJuez,
        boolean condenado,
        int mesesCarcel) {
}
