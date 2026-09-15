package grupog.sistemapolicia.service;

import grupog.sistemapolicia.model.Asalto;
import grupog.sistemapolicia.model.CasoJudicial;
import grupog.sistemapolicia.model.Juez;
import grupog.sistemapolicia.repository.AsaltoRepository;
import grupog.sistemapolicia.repository.CasoJudicialRepository;
import grupog.sistemapolicia.repository.JuezRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CasoJudicialService {

    private final CasoJudicialRepository casoRepository;
    private final AsaltoRepository asaltoRepository;
    private final JuezRepository juezRepository;

    public CasoJudicialService(CasoJudicialRepository casoRepository,
            AsaltoRepository asaltoRepository,
            JuezRepository juezRepository) {
        this.casoRepository = casoRepository;
        this.asaltoRepository = asaltoRepository;
        this.juezRepository = juezRepository;
    }

    public List<CasoJudicial> listar() {
        return casoRepository.findAll();
    }

    public List<CasoJudicial> listarDetenidos() {
        return casoRepository.findByCondenadoTrue();
    }

    @Transactional
    public CasoJudicial guardar(CasoJudicial caso) {
        validar(caso);

        String idAsalto = caso.getAsalto().getIdAsalto();
        String claveJuez = caso.getJuez().getClaveInterna();

        if (casoRepository.existsById(idAsalto)) {
            throw new IllegalArgumentException(
                    "Ya existe un caso judicial para el asalto '"
                    + idAsalto + "'");
        }

        Asalto asalto = asaltoRepository.findById(idAsalto)
                .orElseThrow(() -> new IllegalArgumentException(
                "No existe el asalto '" + idAsalto + "'"));

        Juez juez = juezRepository.findById(claveJuez)
                .orElseThrow(() -> new IllegalArgumentException(
                "No existe el juez '" + claveJuez + "'"));

        caso.setAsalto(asalto);
        caso.setJuez(juez);
        if (!caso.isCondenado()) {
            caso.setMesesCarcel(0);
        }
        return casoRepository.save(caso);
    }

    private void validar(CasoJudicial caso) {
        if (caso == null) {
            throw new IllegalArgumentException(
                    "Los datos del caso judicial son obligatorios");
        }
        if (caso.getAsalto() == null
                || caso.getAsalto().getIdAsalto() == null
                || caso.getAsalto().getIdAsalto().isBlank()) {
            throw new IllegalArgumentException(
                    "El identificador del asalto es obligatorio");
        }
        if (caso.getJuez() == null
                || caso.getJuez().getClaveInterna() == null
                || caso.getJuez().getClaveInterna().isBlank()) {
            throw new IllegalArgumentException(
                    "La clave del juez es obligatoria");
        }
        if (caso.isCondenado() && caso.getMesesCarcel() <= 0) {
            throw new IllegalArgumentException(
                    "Una condena debe indicar meses de cárcel mayores a cero");
        }
    }
}
