package prog2.policia_backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import prog2.policia_backend.DTOs.BandaDTO;
import prog2.policia_backend.exceptions.BandaConMiembrosException;
import prog2.policia_backend.exceptions.BandaYaActivaException;
import prog2.policia_backend.exceptions.BandaYaInactivaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Banda;
import prog2.policia_backend.repositories.BandaRepository;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.utils.GeneradorCodigo;

@Service
@RequiredArgsConstructor
public class BandaService {

    private final BandaRepository bandaRepository;
    private final AsaltanteRepository asaltanteRepository;

    public List<BandaDTO> listar(Boolean activo) {

        List<Banda> bandas;

        if (activo == null) {
            bandas = bandaRepository.findAll();
        } else {
            bandas = bandaRepository.findByActivo(activo);
        }

        List<BandaDTO> resultado = new ArrayList<>();

        for (Banda banda : bandas) {
            resultado.add(convertirADTO(banda));
        }

        return resultado;
    }

    public BandaDTO buscarPorCodigo(String codigo) {
        return convertirADTO(obtenerBanda(codigo));
    }

    @Transactional
    public BandaDTO guardar() {
        Banda banda = new Banda();

        banda = bandaRepository.save(banda);

        banda.setCodigo(GeneradorCodigo.generar("BAN", banda.getId()));
        banda.setActivo(true);

        banda = bandaRepository.save(banda);

        return convertirADTO(banda);
    }

    public void eliminar(String codigo) {
        Banda banda = obtenerBanda(codigo);

        if (!banda.isActivo()) {
            throw new BandaYaInactivaException();
        }

        if (asaltanteRepository.existsByBanda_IdAndActivoTrue(banda.getId())) {
            throw new BandaConMiembrosException();
        }

        banda.setActivo(false);
        bandaRepository.save(banda);
    }

    public BandaDTO reactivar(String codigo) {
        Banda banda = obtenerBanda(codigo);

        if (banda.isActivo()) {
            throw new BandaYaActivaException();
        }

        banda.setActivo(true);

        banda = bandaRepository.save(banda);

        return convertirADTO(banda);
    }

    private BandaDTO convertirADTO(Banda banda) {
        return new BandaDTO(
                banda.getId(),
                banda.getCantMiembros(),
                banda.getCodigo(),
                banda.isActivo(),
                banda.getFechaCreacion(),
                banda.getFechaModificacion(),
                banda.getCreadoPor(),
                banda.getModificadoPor());
    }

    // -----------Métodos Aux---
    private Banda obtenerBanda(String codigo) {
        Optional<Banda> resultado = bandaRepository.findByCodigo(codigo);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Banda", codigo);
        }

        return resultado.get();
    }
}
